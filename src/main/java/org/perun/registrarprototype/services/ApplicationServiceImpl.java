package org.perun.registrarprototype.services;

import cz.metacentrum.perun.openapi.model.AttributeDefinition;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.perun.registrarprototype.events.ApplicationApprovedEvent;
import org.perun.registrarprototype.events.ApplicationItemVerificationRequiredEvent;
import org.perun.registrarprototype.events.ApplicationRejectedEvent;
import org.perun.registrarprototype.events.ApplicationSubmittedEvent;
import org.perun.registrarprototype.events.ApplicationVerifiedEvent;
import org.perun.registrarprototype.events.ChangesRequestedToApplicationEvent;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;
import org.perun.registrarprototype.exceptions.SimilarIdentitiesFoundException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.ApplicationForm;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.models.SubmissionContext;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.models.ValidationError;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.DecisionRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.SubmissionRepository;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.perun.registrarprototype.services.prefillStrategy.impl.PrefillStrategyResolver;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {
  public static final String IDENTIFIER_CLAIM = "sub";
  public static final String ISSUER_CLAIM = "iss";
  private final ApplicationRepository applicationRepository;
  private final FormRepository formRepository;
  private final SubmissionRepository submissionRepository;
  private final DecisionRepository decisionRepository;
  private final EventService eventService;
  private final AuthorizationService authorizationService;
  private final FormService formService;
  private final IdMService idmService;
  private final SessionProvider sessionProvider;
  private final PrefillStrategyResolver prefillStrategyResolver;

  public ApplicationServiceImpl(ApplicationRepository applicationRepository, FormRepository formRepository,
                                SubmissionRepository submissionRepository, DecisionRepository decisionRepository,
                                EventService eventService,
                                AuthorizationService authorizationService, FormService formService,
                                IdMService idmService, SessionProvider sessionProvider,
                                PrefillStrategyResolver prefillStrategyResolver) {
    this.applicationRepository = applicationRepository;
    this.formRepository = formRepository;
    this.submissionRepository = submissionRepository;
    this.decisionRepository = decisionRepository;
    this.eventService = eventService;
    this.authorizationService = authorizationService;
    this.formService = formService;
    this.idmService = idmService;
    this.sessionProvider = sessionProvider;
    this.prefillStrategyResolver = prefillStrategyResolver;
  }

  @Override
  public Application approveApplication(RegistrarAuthenticationToken sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));

    List<AssignedFormModule> modules = formService.getAssignedFormModules(app.getForm());
    for (AssignedFormModule module : modules) {
      module.getFormModule().beforeApproval(app);
    }

    if (!authorizationService.canDecide(sess, app.getForm().getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to approve this application");
    }

    makeDecision(sess.getPrincipal(), app, message, Decision.DecisionType.APPROVED);

    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    // user could have consolidated identities in perun in time between submission and approval
    if (app.getIdmUserId() == null) {
      Integer perunUserId = idmService.getUserIdByIdentifier(app.getSubmission().getIdentityIdentifier());
      app.setIdmUserId(perunUserId);
    }

    app = applicationRepository.save(app);

    Integer idmUserId = propagateApprovalToIdm(app);
    // consolidate all applications with the potentially newly created idmUserId
    consolidateSubmissions(idmUserId, app.getSubmission().getIdentityIdentifier(), app.getSubmission().getIdentityIssuer());

    for (AssignedFormModule module : modules) {
      module.getFormModule().onApproval(app);
    }

    eventService.emitEvent(new ApplicationApprovedEvent(app));
    return app;
  }

  /**
   * Propagates the approval to the IdM, adds the applicant to the desired object. Returns ID of IdM user.
   * @param application
   * @return
   */
  private Integer propagateApprovalToIdm(Application application) {
    // TODO unreserve logins

    if (application.getType().equals(FormSpecification.FormType.INITIAL)) {
      if (application.getIdmUserId() == null) {
        return idmService.createMemberForCandidate(application);
      }
      return idmService.createMemberForUser(application);
    } else if (application.getType().equals(FormSpecification.FormType.EXTENSION)) {
      return idmService.extendMembership(application);
    }
    // TODO prolly should not happen
    return null;
  }

  /**
   * Set newly retrieved attributes from application acceptance to existing open applications
   */
  private void consolidateSubmissions(Integer idmUserId, String identityIdentifier, String identityIssuer) {
    List<Submission> submissions = submissionRepository.findAllByIdentifierAndIssuer(identityIdentifier, identityIssuer);
    submissions.forEach(submission -> {
      submission.setSubmitterId(idmUserId);
      submission.getApplications().forEach(application -> application.setIdmUserId(idmUserId));
      applicationRepository.updateAll(submission.getApplications());
    });
    submissionRepository.updateAll(submissions);
  }

  @Override
  public Application rejectApplication(RegistrarAuthenticationToken sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));

    if (!authorizationService.canDecide(sess, app.getForm().getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to reject this application");
    }

    makeDecision(sess.getPrincipal(), app, message, Decision.DecisionType.REJECTED);

    try {
      app.reject("Manually rejected by manager");
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    // TODO unreserve logins -> this probably also needs to be done in IdM/adapter (shortly before setting the attributes)

    // TODO how do we handle auto-submitted applications related to this one?

    List<AssignedFormModule> modules = formService.getAssignedFormModules(app.getForm());
    for (AssignedFormModule module : modules) {
      module.getFormModule().onRejection(app);
    }

    app = applicationRepository.save(app);
    eventService.emitEvent(new ApplicationRejectedEvent(app));

    return app;
  }

  @Override
  public Application requestChanges(RegistrarAuthenticationToken sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId)
                          .orElseThrow(() -> new IllegalArgumentException("Application not found"));

    if (StringUtils.isEmpty(message)) {
      throw new IllegalArgumentException("Cannot request changes without a message");
    }

    makeDecision(sess.getPrincipal(), app, message, Decision.DecisionType.CHANGES_REQUESTED);

    try {
      app.requestChanges();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    app = applicationRepository.save(app);
    eventService.emitEvent(new ChangesRequestedToApplicationEvent(app));

    return app;
  }

  /**
   * Creates the decision object filled with metadata
   * @param principal
   * @param app
   * @param message
   * @param decisionType
   * @return
   */
  private Decision makeDecision(CurrentUser principal, Application app, String message, Decision.DecisionType decisionType) {
    Decision decision = new Decision();
    decision.setApplication(app);
    decision.setApproverId(principal.id());
    decision.setApproverName(principal.name());
    decision.setDecisionType(decisionType);
    decision.setMessage(message);
    decision.setTimestamp(LocalDateTime.now());
    return decisionRepository.save(decision);
  }

  @Override
  public SubmissionResult applyForMemberships(SubmissionContext submissionData) {
    Submission submission = new Submission();
    List<Application> applications = new ArrayList<>();

    for (ApplicationForm appContext : submissionData.getPrefilledData()) {
      Application app = applyForMembership(appContext, submission, submissionData.getRedirectUrl());
      applications.add(app);
    }

    submission.setApplications(applications);

    setSubmissionMetadata(submission);
    submission = submissionRepository.save(submission);

    SubmissionResult result = new SubmissionResult();
    result.setSubmission(submission);

//    List<Form> autoSubmitForms = submissionData.getPrefilledData().stream()
//                                    .flatMap(formData -> formService.getAutosubmitForms(formData.getForm(), formData.getType()).stream())
//                                     .toList();
//    autoSubmitForms.forEach(form -> {
//      autoSubmitForm(sess, form, submissionData);
//      result.addMessage("Form into group " + form.getGroupId() + " was submitted automatically.");
//    });
//
//    List<Form> redirectForms = submissionData.getPrefilledData().stream()
//                                   .flatMap(formData -> formService.getRedirectForms(formData.getForm(), formData.getType()).stream())
//                                    .toList();
//    result.setRedirectUrl(submissionData.getRedirectUrl());
//    setRedirectForms(sess, redirectForms, result);

    result.addMessage("Successfully submitted applications"); // TODO add custom form messages / is this really part of the domain => depends on if we consider the result page as part of the flow YES

    return result;
  }

  /**
   * Fills the submission object with metadata used to associate different submissions with the same user, display
   * information about the user in application detail, etc.
   * TODO inquire which claims/attributes to store
   * @param submission
   */
  private void setSubmissionMetadata(Submission submission) {
    submission.setTimestamp(LocalDateTime.now());
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();
    if (!sess.isAuthenticated()) {
      return;
    }

    submission.setSubmitterId(sess.getPrincipal().id()); // defaults to null if perun user id not present
    submission.setIdentityIdentifier(sess.getPrincipal().attribute("sub"));
    submission.setIdentityIssuer(sess.getPrincipal().attribute("iss"));
    submission.setSubmitterName(sess.getPrincipal().name());
    // TODO inquire which claims/attributes to store

    submission.setIdentityAttributes(sess.getPrincipal().getAttributes().entrySet().stream()
                                         .filter(e -> e.getValue() != null)
                                         .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));

  }

  @Override
  public Application applyForMembership(ApplicationForm applicationForm, Submission submission, String redirectUrl) {
    FormSpecification formSpecification = formRepository.findById(applicationForm.getForm().getId()).orElseThrow(() -> new IllegalArgumentException("Form not found"));
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();

    if (applicationForm.getType() == FormSpecification.FormType.EXTENSION && !canExtendMembership(applicationForm.getForm()
                                                                                            .getGroupId())) {
      throw new IllegalArgumentException("User cannot extend membership in group: " + applicationForm.getForm()
                                                                                          .getGroupId());
    }

    if (checkOpenApplications(sess, formSpecification) != null && applicationForm.getType() != FormSpecification.FormType.UPDATE) {
      // user has applied for membership since loading the form
      throw new IllegalArgumentException("User already has an open application in group: " + applicationForm.getForm()
                                                                                                 .getGroupId());
    }


    validateFilledFormData(applicationForm);
    normalizeFilledFormData(applicationForm);
    applicationForm.getPrefilledItems().forEach(item -> checkPrefilledValueConsistency(sess, item));

    Application app = new Application(0, sess.getPrincipal().id(), formSpecification,
        applicationForm.getPrefilledItems(), null, applicationForm.getType());
    app.setRedirectUrl(redirectUrl);
    app.setSubmission(submission);
    try {
      app.submit(formSpecification);
    } catch (InvalidApplicationStateTransitionException e) {
      // should not happen, we checked the items beforehand
      throw new DataInconsistencyException(e.getMessage());
    }

    app = applicationRepository.save(app);

    reserveLogins(applicationForm.getPrefilledItems());

    // TODO if we emit events asynchronously this might be problematic (not sure rollback would work here)
    eventService.emitEvent(new ApplicationSubmittedEvent(app));

    attemptApplicationVerification(app);

    return app;
  }

  @Override
  public List<Application> getAllApplications() {
    return applicationRepository.findAll();
  }

  @Override
  public Application getApplicationById(int id) {
    return applicationRepository.findById(id).orElse(null);
  }

  @Override
  public List<Decision> getDecisionsByApplicationId(int applicationId) {
    return decisionRepository.findByApplicationId(applicationId);
  }

  @Override
  public Decision getLatestDecisionByApplicationId(int applicationId) {
    return decisionRepository.findByApplicationId(applicationId).stream()
        .max((d1, d2) -> {
          if (d1.getTimestamp() == null && d2.getTimestamp() == null) return 0;
          if (d1.getTimestamp() == null) return -1;
          if (d2.getTimestamp() == null) return 1;
          return d1.getTimestamp().compareTo(d2.getTimestamp());
        })
        .orElse(null);
  }

  /**
   * Validates that the submitted form data is correctly filled in (e.g. required items not empty, values match
   * constraints, etc.)
   * @param data
   */
  private void validateFilledFormData(ApplicationForm data) {
    // TODO consider checking whether DISABLED/HIDDEN items were not filled (or simply skip them)
    List<FormItem> items = formService.getFormItems(data.getForm(), data.getType());

    Set<Integer> itemIds = items.stream().map(FormItem::getId).collect(Collectors.toSet());
    List<FormItemData> foreignItems = data.getPrefilledItems().stream()
                                          .filter((itemData) -> !itemIds.contains(itemData.getFormItem().getId()))
                                          .toList();
    if (!foreignItems.isEmpty()) {
      throw new DataInconsistencyException("Submitted form: " + data.getForm().getId() + " contains data for items" +
                                               " not currently in that form: " + foreignItems);
    }

    List<ValidationError> result = items.stream()
                                       .map(item -> item.validate(
        data.getPrefilledItems().stream()
            .filter(itemData -> itemData.getFormItem().getId() == item.getId())
            .findFirst().orElse(null))
        )
                                       .filter(Objects::nonNull).toList();
    if (!result.isEmpty()) {
      throw new InvalidApplicationDataException("Some of the form items were incorrectly filled in",
            result, null);
    }
  }

  /**
   * Fix data format
   * @param data
   */
  private void normalizeFilledFormData(ApplicationForm data) {

  }

  /**
   * Checks that prefilled data is still valid (e.g.
   * the submitted value matches prefilled value from source attributes), if so set a flag to indicate that the form item
   * data has been validated (LOA or just a flag).
   * TODO do we want the option to require item value to be validated? This can optionally be done using `beforeApproval` module hook
   * @param sess
   * @param itemData
   */
  private void checkPrefilledValueConsistency(RegistrarAuthenticationToken sess, FormItemData itemData) {
    itemData.setValueAssured(false);
    if (StringUtils.isEmpty(itemData.getValue())) {
      return;
    }

    if (!sess.isAuthenticated()) {
      return;
    }

    PrefillStrategy itemPrefillStrategy = prefillStrategyResolver.resolveFor(itemData.getFormItem());
    itemPrefillStrategy.validateOptions(itemData.getFormItem().getPrefillStrategyOptions());
    Optional<String> prefilledValue = itemPrefillStrategy.prefill(itemData.getFormItem(),
        itemData.getFormItem().getPrefillStrategyOptions());
    if (prefilledValue.isEmpty()) {
      return;
    }

    if (prefilledValue.get().equals(itemData.getValue())) {
      itemData.setValueAssured(true);
    }
  }

  //TODO run this asynchronously?
  @Override
  public void autoSubmitForm(FormTransition autoSubmitTransition, SubmissionContext submissionData) {

    SubmissionContext autoSubmitData;
    try {
      FormSpecification.FormType type = selectFormType(new Requirement(autoSubmitTransition.getTargetForm().getGroupId(),
          autoSubmitTransition.getTargetFormState()));
      if (type == null) {
        System.out.println("Requirement for auto submit " + autoSubmitTransition.getTargetForm().getGroupId() + " already fulfilled");
        return;
      }
      autoSubmitData = prepareApplicationForms(Map.of(autoSubmitTransition.getTargetForm(), type), null);
      applyForMemberships(autoSubmitData);
    } catch (Exception e) {
      System.out.println("Error while auto-submitting application for form " + autoSubmitTransition.getTargetForm().getGroupId() + " with error: " + e.getMessage()); // TODO log properly
      // consider adding error into result messages?
    }
  }

  @Override
  //@Transactional
  public void updateApplicationData(int applicationId, List<FormItemData> itemData) {
    Application application = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));

    List<FormItemData> existingData = application.getFormItemData();
    // todo this will likely change with data persistence
    Map<FormItem, FormItemData> existingItemToDataMap = existingData.stream()
                                                             .collect(Collectors.toMap(FormItemData::getFormItem, item -> item));

    if (!application.getState().isOpenState()) {
      throw new IllegalArgumentException("Only open applications can be updated");
    }

    if (itemData == null || itemData.isEmpty()) {
      return;
    }

    itemData.forEach(item -> {
      if (!existingItemToDataMap.containsKey(item.getFormItem())) {
        throw new IllegalArgumentException("Item " + item.getFormItem().getId() + " does not belong to application " + applicationId);
      }
      if (!item.getFormItem().isUpdatable()) {
        throw new IllegalArgumentException("Item " + item.getFormItem().getId() + " cannot be updated");
      }

      FormItemData existingItem = existingItemToDataMap.get(item.getFormItem());

      existingItem.setValue(item.getValue());

      if (item.getFormItem().getType().isVerifiedItem()) {
        checkPrefilledValueConsistency(sessionProvider.getCurrentSession(), existingItem);
      }
    });

    validateFilledFormData(application);

    // set to submitted (if from changes requested for example)
    application.setState(ApplicationState.SUBMITTED);

    attemptApplicationVerification(application);

    applicationRepository.save(application);
  }



  /**
   * Auto-fill items of an automatically submitted application using the data of the set of submitted applications
   * that triggered the auto-submission.
   * TODO do we match based on identical/similar form item names, define an exact mapping between forms (e.g. form item id to form item id)
   *   or just fill items with same source/destination attribute names + metadata from principal?
   *   Prefill using the classic submission flow (in case the auto form has more prefilled items) / use just data from the previous submission (fewer IdM calls)?
   *   What if user rewrote prefilled values in the application?
   * @param sess
   * @param formSpecification
   * @param prefilledItems
   */
  private ApplicationForm prefillAutosubmitFormItems(CurrentUser sess, FormSpecification formSpecification, List<FormItemData> prefilledItems) {
    return null;
  }

  /**
   * Check whether all items of which values need to be assured, are assured
   * @param application
   * @return
   */
  private void attemptApplicationVerification(Application application) {
    List<FormItemData> unassuredItems = application.getFormItemData().stream()
        .filter(itemData -> itemData.getFormItem().getType().isVerifiedItem())
        .filter(itemData -> !itemData.isValueAssured()).toList();

    if (unassuredItems.isEmpty()) {
      application.setState(ApplicationState.VERIFIED);
      eventService.emitEvent(new ApplicationVerifiedEvent(application));
      // TODO technically auto-approval could be a part of the event handler, which would have its own principal?
      attemptAutoApproval(application);
    } else {
      eventService.emitEvent(new ApplicationItemVerificationRequiredEvent(application, unassuredItems));
    }
  }

  /**
   *
   * @param application
   */
  private void attemptAutoApproval(Application application) {
    // TODO implement auto approval logic (prolly some better way than just using a fake/engine principal)
  }

  /**
   * Tries to load prefilled data for redirect forms. If this fails for whatever reason, log the error and (potentially)
   * return it via the submission result messages.
   * @param sess
   * @param redirectTransitions
   * @return
   */
  private void setRedirectForms(CurrentUser sess, List<FormTransition> redirectTransitions, SubmissionResult submissionResult) {
    if (!redirectTransitions.isEmpty()) {
      // TODO do we want to load prerequisites of redirect forms like so?
      List<Requirement> requirements = redirectTransitions.stream()
                                           .map(transition -> new Requirement(transition.getTargetForm().getGroupId(),
                                               transition.getTargetFormState())).toList();
      try {
        submissionResult.setRedirectForms(loadForms(requirements, submissionResult.getRedirectUrl(), false));
        // theoretically just the set of ids (or whatever the API will take as the entrypoint of registration flow) is enough and just redirect user to that endpoint
      } catch (Exception e) {
        System.out.println("Error assembling redirect forms: " + e.getMessage()); // TODO log properly
        // probably best to use some constant here so that it can be localized
        // submissionResult.addMessage("Error assembling redirect forms: " + e.getMessage());
        submissionResult.addMessage("Could not redirect to another form. Please contact support.");
      }
    }
  }

  //TODO should be enough to build the whole form page? or do we need more info?
  @Override
  public SubmissionContext loadForms(List<Requirement> requirements, String redirectUrl, boolean checkSimilarUsers) {
    if (checkSimilarUsers) { // potentially enable/disable with config
      // Check this once when loading registrar for the first time -> offer user the option to consolidate, do not check again afterwards
      List<Identity> similarIdentities =
          idmService.checkForSimilarUsers((String) sessionProvider.getCurrentSession().getCredentials());
      if (!similarIdentities.isEmpty()) {
        // handle this in gui to display message and offer consolidator redirect
        throw new SimilarIdentitiesFoundException("Found similar identities: " + similarIdentities);
      }
    }

    List<Requirement> prerequisites = new ArrayList<>();

    requirements.forEach(requirement -> prerequisites.addAll(getPrerequisiteRequirements(requirement, new ArrayList<>())));

    requirements.addAll(prerequisites);

    // Reversed should make it so that the prerequisites are in correct order
    // TODO do we want to keep information about what forms are prerequisites for?
    Map<FormSpecification, FormSpecification.FormType> requiredForms = determineFormSpecification(requirements.reversed());
    return prepareApplicationForms(requiredForms, redirectUrl);
  }

  /**
   * Determines the application types for each form, retrieves and prefills items for that form type, and returns this
   * aggregated data.
   * @param formSpecsWithType
   * @param redirectUrl
   * @return
   */
  private SubmissionContext prepareApplicationForms(Map<FormSpecification, FormSpecification.FormType> formSpecsWithType, String redirectUrl) {
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();
    List<ApplicationForm> requiredForms = new ArrayList<>();
    for (Map.Entry<FormSpecification, FormSpecification.FormType> requiredForm : formSpecsWithType.entrySet()) {
      FormSpecification formSpecification = requiredForm.getKey();
      FormSpecification.FormType type = requiredForm.getValue();
      Application openApp = checkOpenApplications(sess, formSpecification);
      if (openApp != null) {
        if (openApp.getState().equals(ApplicationState.CHANGES_REQUESTED)) {
          // TODO we want to indicate that the application exists, but there are changes requested -> display it
          continue;
        }
        // TODO here we want to indicate that the application exists, just hasn't been accepted yet -> no need to display it?
        continue;
      }

      formSpecification.setItems(formService.getFormItems(formSpecification, type));

      List<FormItemData> prefilledFormItemData = loadForm(sess, formSpecification, type);

      requiredForms.add(new ApplicationForm(formSpecification, prefilledFormItemData, type));
    }

    // TODO probably some logic to order the individual forms? (e.g. prerequisites before their source form - or do we want
    //  to display prerequisites independently and have them redirect to the original forms?
    return new SubmissionContext(redirectUrl, requiredForms);
  }

  /**
   * For the group associated with the given form, determine the FormType (e.g. INITIAL, EXTENSION)
   * @param requirement
   * @return
   */
  private FormSpecification.FormType selectFormType(Requirement requirement) {
    switch (requirement.getTargetState()) {
      case MEMBER -> {
          if (checkUserMembership(requirement.getGroupId())) {
            if (canExtendMembership(requirement.getGroupId())) {
              return FormSpecification.FormType.EXTENSION;
            }
            // todo display error message if no forms returned?
            return null;
          }
          return FormSpecification.FormType.INITIAL;
      }
      case NOT_MEMBER -> {
          if (checkUserMembership(requirement.getGroupId())) {
            return FormSpecification.FormType.CANCELLATION;
          }
          return null;
      }
      case FRESH_ATTR_VALUE -> {
        if (checkUserMembership(requirement.getGroupId())) {
          return FormSpecification.FormType.UPDATE;
        }
        return null;
      }
      default -> throw new IllegalArgumentException("Unsupported target state: " + requirement.getTargetState());
    }
  }


  @Override
  public List<FormItemData> loadForm(RegistrarAuthenticationToken sess, FormSpecification formSpecification, FormSpecification.FormType type) {
//    if (type.equals(Form.FormType.UPDATE)) {
//      // TODO prefill form with data from already submitted app if loading modifying/update form type
//    }

    List<AssignedFormModule> modules = formService.getAssignedFormModules(formSpecification);
    modules.forEach(module -> module.getFormModule().canBeSubmitted(sess.getPrincipal(), type, module.getOptions()));

    List<FormItemData> prefilledFormItemData = prefillForm(sess, formSpecification);

    modules.forEach(module -> module.getFormModule().afterFormItemsPrefilled(sess.getPrincipal(), type, prefilledFormItemData));

    checkMissingPrefilledItems(sess, prefilledFormItemData);

    return prefilledFormItemData;
  }

  @Override
  public List<Identity> checkForSimilarIdentities(List<FormItemData> itemData) {
    return idmService.checkForSimilarUsers((String) sessionProvider.getCurrentSession().getCredentials(), itemData);
  }

  @Override
  public List<Application> getApplicationsForForm(int formId, List<ApplicationState> states) {
    if (states == null || states.isEmpty()) {
      return applicationRepository.findByFormId(formId);
    }
    return applicationRepository.findByFormId(formId).stream()
               .filter(app -> states.contains(app.getState()))
               .toList();
  }

  /**
   * Returns prefilled form item data for supplied form's items.
   * Allow admin to define where to fill item from (e.g. federation, IdM, more complex solutions/external sources -> from submitted prerequisites)]
   * TODO we can also hide prerequisite submitted apps behind external sources (same with redirect forms)
   * @param sess
   * @param formSpecification Form object with set form items array
   * @return
   */
  private List<FormItemData> prefillForm(RegistrarAuthenticationToken sess, FormSpecification formSpecification) {

    return formSpecification.getItems().stream()
        .map((item) -> new FormItemData(item, null,
            calculatePrefilledValue(sess, item)))
        .toList();
    // TODO consider prefilling from submitted prerequisite forms (from submitted matching destination attribute to source/destination?)
    //  => only if explicitly defined as source + that's where a key of source item is defied
  }


  /**
   * Ensures that all items that are required and do not allow user input are prefilled.
   * TODO move this logic to setting up form => ensure that admin cannot create a form that would result in a situation like this
   * @param sess
   * @param prefilledItems
   */
  private void checkMissingPrefilledItems(RegistrarAuthenticationToken sess, List<FormItemData> prefilledItems) {
    List<FormItemData> unmodifiableRequiredButEmpty = new ArrayList<>();
    List<FormItemData> itemsWithMissingData = new ArrayList<>();
    prefilledItems.stream()
        .filter(item -> hasItemIncorrectVisibility(item, prefilledItems)).forEach(item -> {
          if (item.getFormItem().getPrefillStrategyTypes() == null ||
              item.getFormItem().getPrefillStrategyTypes().isEmpty()) {
            unmodifiableRequiredButEmpty.add(item);
          } else {
            itemsWithMissingData.add(item);
          }
        });
    if (!unmodifiableRequiredButEmpty.isEmpty()) {
      System.out.println("ERROR: The following items were set as hidden or disabled but do not have source attributes" +
                             " to prefill from: " + unmodifiableRequiredButEmpty);
      throw new RuntimeException("Items that are hidden/disabled but do not have a source attribute should not exist:" +
                                     unmodifiableRequiredButEmpty);
    }
    if (!itemsWithMissingData.isEmpty()) {
      System.out.println("ERROR: Could not prefill the following disabled/hidden attributes: " + itemsWithMissingData);
      if (sess.isAuthenticated()) {
        // user is authenticated, hence source attribute values should exist?
        throw new RuntimeException("Could not prefill the following disabled/hidden attributes: " + itemsWithMissingData);
      }
    }
  }

  /**
   * Checks whether there is an issue with the item's visibility/editable settings.
   * @param item
   * @param prefilledFormItemData
   * @return
   */
  private boolean hasItemIncorrectVisibility(FormItemData item, List<FormItemData> prefilledFormItemData) {
    return item.getFormItem().isRequired() && StringUtils.isEmpty(item.getPrefilledValue()) &&
            (isItemHidden(item, prefilledFormItemData) || isItemDisabled(item, prefilledFormItemData));
  }

  /**
   * Checks whether the item will be hidden in the form
   * @param item
   * @param prefilledFormItemData
   * @return
   */
  private boolean isItemHidden(FormItemData item, List<FormItemData> prefilledFormItemData) {
    return isItemConditionApplied(item.getPrefilledValue(), prefilledFormItemData, item.getFormItem().getHidden(),
        item.getFormItem().getHiddenDependencyItemId());
  }

  /**
   * Checks whether the item will be disabled in the form
   * @param item
   * @param prefilledFormItemData
   * @return
   */
  private boolean isItemDisabled(FormItemData item, List<FormItemData> prefilledFormItemData) {
    return isItemConditionApplied(item.getPrefilledValue(), prefilledFormItemData, item.getFormItem().getDisabled(),
        item.getFormItem().getDisabledDependencyItemId());
  }

  /**
   * Checks whether the supplied condition will be applied for the item
   * @param valueToCheck
   * @param prefilledFormItemData
   * @param condition
   * @param dependencyItemId
   * @return
   */
  private boolean isItemConditionApplied(String valueToCheck, List<FormItemData> prefilledFormItemData,
                                         FormItem.Condition condition, Integer dependencyItemId) {
    if (dependencyItemId != null) {
      FormItemData dependentItem = prefilledFormItemData.stream().
                                       filter(otherItem -> otherItem.getFormItem().getId() == dependencyItemId)
                                       .findFirst()
                                       .orElseThrow(() -> new RuntimeException("Dependent item " + dependencyItemId + " not found"));
      valueToCheck = dependentItem.getPrefilledValue();
    }

    return switch (condition) {
      case ALWAYS -> true;
      case NEVER -> false;
      case IF_PREFILLED -> StringUtils.isNotBlank(valueToCheck);
      case IF_EMPTY -> StringUtils.isBlank(valueToCheck);
    };
  }

  /**
   * Prefill form item value based on principal data (to be returned to GUI).
   * @param item
   * @return
   */
  private String calculatePrefilledValue(RegistrarAuthenticationToken sess, FormItem item) { // again decide whether to pass principal as argument or retrieve it from the current session

    if (!sess.isAuthenticated()) {
      // TODO potentially move this to the strategy logic? Could there be a strategy that returns value for unauthenticated users?
      return item.getDefaultValue();
    }

    PrefillStrategy itemPrefillStrategy = prefillStrategyResolver.resolveFor(item);
    itemPrefillStrategy.validateOptions(item.getPrefillStrategyOptions());
    Optional<String> prefilledValue = itemPrefillStrategy.prefill(item, item.getPrefillStrategyOptions());

    return prefilledValue.orElseGet(item::getDefaultValue);

  }

  /**
   * Reserves the login in the set namespace
   * TODO this has to be implemented using IdM calls -> how do we ensure that IdM does not create a user with the reserved
   *  login (e.g. using service accounts).
   * @param itemData
   */
  private void reserveLogins(List<FormItemData> itemData) {

    for (FormItemData formItemData : itemData) {
      if (formItemData.getFormItem().getType().equals(FormItem.Type.LOGIN)) {
        if (StringUtils.isEmpty(formItemData.getValue()) ||
                formItemData.isValueAssured()) { // login with prefilled value => value alr reserved
          continue;
        }
        // could be a lot of calls
        AttributeDefinition attrDef = idmService.getAttributeDefinition(formItemData.getFormItem().getDestinationIdmAttribute());
        if (attrDef == null) {
          // TODO probably throw an error here
          continue;
        }
        String namespace =  attrDef.getNamespace();
        if (idmService.isLoginAvailable(namespace, formItemData.getValue())) {
          // these two calls can probably be joined
          idmService.reserveLogin(namespace, formItemData.getValue());
        } else {
          throw new RuntimeException("Login " + formItemData.getValue() + " is blocked");
        }
      }
    }
  }

  /**
   * Check whether user already has open application in the given group/VO (for the given form). If so, use this information
   * to determine FormType (e.g., if user has open application, update this application -  TODO do we use separate application type or just update the data?)
   * TODO also check by data stored in the Submission object
   * @param formSpecification
   */
  private Application checkOpenApplications(RegistrarAuthenticationToken sess, FormSpecification formSpecification) {
    if (sess.isAuthenticated()) {
      // TODO we need to have identities consolidated before this point
      Optional<Application> foundApp = applicationRepository.findByFormId(formSpecification.getId()).stream()
          .filter(app -> app.getState().isOpenState())
          .filter(app -> (app.getIdmUserId() != null && Objects.equals(app.getIdmUserId(), sess.getPrincipal().id())) ||
                             (Objects.equals(app.getSubmission().getIdentityIssuer(),
                                 sess.getPrincipal().attribute(ISSUER_CLAIM))) &&
                                 app.getSubmission().getIdentityIdentifier().equals(sess.getPrincipal().attribute(
                                     IDENTIFIER_CLAIM)))
                                .findFirst(); // TODO utilize similarUsers as well? prolly make IdM consolidate first?
      return foundApp.orElse(null);
    }
    return null;
  }

  /**
   * Check whether user is a member of the group/VO in the underlying IdM. If so, use this information to determine
   * FormType (e.g if user is member and is can extend, use EXTENSION form type) TODO UPDATE and CANCELLATION form types via different endpoint?
   * @param groupId
   */
  private boolean checkUserMembership(int groupId) {
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();
    if (sess.isAuthenticated()) {
      // TODO remove once we have IdM methods to check membership
      return sess.getPrincipal().getRoles().get(Role.MEMBERSHIP).contains(groupId);
      // return idmService.getGroupIdsWhereUserIsMember(sess.getPrincipal().id()).contains(groupId);
    }
    return false;
  }

  /**
   * Check whether user can extend membership in the group/VO in the underlying IdM.
   * TODO Either use a direct IdM call or operate using membershipExpiration attribute? Prolly direct call, more logic involved (rules, etc.)
   * operate using
   * @param groupId
   * @return
   */
  private boolean canExtendMembership(int groupId) {
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();
    if (sess.isAuthenticated() && checkUserMembership(groupId)) {
      return idmService.canExtendMembership(sess.getPrincipal().id(), groupId);
    }
    return false;
  }

  /**
   * Determine which groups to submit applications to based on the input parameters.
   * Meaning determine prerequisites for group based on form relationships, based on the input parameters (can be a list of ids/names)
   * @param requirements requirements
   * @return list of group ids to submit applications to
   */
  private Map<FormSpecification, FormSpecification.FormType> determineFormSpecification(List<Requirement> requirements) {
    Map<FormSpecification, FormSpecification.FormType> requiredForms = new HashMap<>();
    for (Requirement requirement : requirements) {
      FormSpecification formSpecification = formRepository.findByGroupId(requirement.getGroupId())
                      .orElseThrow(() -> new IllegalArgumentException("Form for group " + requirement.getGroupId() + " not found"));
      // TODO do we always display EXTENSION forms if user is already member of some groups?

      FormSpecification.FormType type = selectFormType(requirement);
      if (type == null) {
        continue;
      }

      requiredForms.put(formSpecification, type);
    }
    return requiredForms;
  }

  /**
   * Recursively retrieve the prerequisite requirements.
   * TODO figure out how to order them (is reversing the list at the end enough?)
   * @param requirement
   * @param requirements
   * @return
   */
  private List<Requirement> getPrerequisiteRequirements(Requirement requirement, List<Requirement> requirements) {
    // TODO do we want prerequisites of GROUP MEMBERSHIP or FORM SUBMISSION?
    FormSpecification formSpecification = formRepository.findByGroupId(requirement.getGroupId())
                      .orElseThrow(() -> new IllegalArgumentException("Form for group " + requirement.getGroupId() + " not found"));

    List<FormTransition> prerequisiteTransitions = formService.getPrerequisiteTransitions(formSpecification,
        requirement.getTargetState());

    prerequisiteTransitions.forEach(transition -> {
      Requirement prerequisiteRequirement = new Requirement(transition.getTargetForm().getGroupId(),
          transition.getTargetFormState());
      requirements.add(prerequisiteRequirement);
      requirements.addAll(getPrerequisiteRequirements(prerequisiteRequirement, requirements));
    });

    return requirements;
  }
}
