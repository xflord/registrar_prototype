package org.perun.registrarprototype.services.impl;

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
import org.perun.registrarprototype.services.events.ApplicationApprovedEvent;
import org.perun.registrarprototype.services.events.ApplicationItemVerificationRequiredEvent;
import org.perun.registrarprototype.services.events.ApplicationRejectedEvent;
import org.perun.registrarprototype.services.events.ApplicationSubmittedEvent;
import org.perun.registrarprototype.services.events.ApplicationVerifiedEvent;
import org.perun.registrarprototype.services.events.ChangesRequestedToApplicationEvent;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.EntityNotExistsException;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.exceptions.IdmObjectNotExistsException;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.ApplicationForm;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.models.SubmissionContext;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.models.ValidationError;
import org.perun.registrarprototype.persistence.ApplicationRepository;
import org.perun.registrarprototype.persistence.DecisionRepository;
import org.perun.registrarprototype.persistence.DestinationRepository;
import org.perun.registrarprototype.persistence.FormItemRepository;
import org.perun.registrarprototype.persistence.FormSpecificationRepository;
import org.perun.registrarprototype.persistence.ItemDefinitionRepository;
import org.perun.registrarprototype.persistence.SubmissionRepository;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.ApplicationService;
import org.perun.registrarprototype.services.EventService;
import org.perun.registrarprototype.services.FormService;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.perun.registrarprototype.services.prefillStrategy.impl.PrefillStrategyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

  private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

  public static final String IDENTIFIER_CLAIM = "sub";
  public static final String ISSUER_CLAIM = "iss";
  private final ApplicationRepository applicationRepository;
  private final FormSpecificationRepository formSpecificationRepository;
  private final SubmissionRepository submissionRepository;
  private final DecisionRepository decisionRepository;
  private final EventService eventService;
  private final FormService formService;
  private final IdMService idmService;
  private final SessionProvider sessionProvider;
  private final PrefillStrategyResolver prefillStrategyResolver;
  private final ItemDefinitionRepository itemDefinitionRepository;
  private final DestinationRepository destinationRepository;
  private final FormItemRepository formItemRepository;

  public ApplicationServiceImpl(ApplicationRepository applicationRepository, FormSpecificationRepository formRepository,
                                SubmissionRepository submissionRepository, DecisionRepository decisionRepository,
                                EventService eventService,
                                FormService formService,
                                IdMService idmService, SessionProvider sessionProvider,
                                PrefillStrategyResolver prefillStrategyResolver,
                                ItemDefinitionRepository itemDefinitionRepository,
                                DestinationRepository destinationRepository, FormItemRepository formItemRepository) {
    this.applicationRepository = applicationRepository;
    this.formSpecificationRepository = formRepository;
    this.submissionRepository = submissionRepository;
    this.decisionRepository = decisionRepository;
    this.eventService = eventService;
    this.formService = formService;
    this.idmService = idmService;
    this.sessionProvider = sessionProvider;
    this.prefillStrategyResolver = prefillStrategyResolver;
    this.itemDefinitionRepository = itemDefinitionRepository;
    this.destinationRepository = destinationRepository;
    this.formItemRepository = formItemRepository;
  }

  @Override
  public Application approveApplication(RegistrarAuthenticationToken sess, int applicationId, String message) {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new EntityNotExistsException("Application", applicationId));
    Integer formSpecId = app.getFormSpecificationId();
    FormSpecification formSpec = formSpecificationRepository.findById(formSpecId).orElseThrow(() -> new EntityNotExistsException("FormSpecification", formSpecId));

    List<AssignedFormModule> modules = formService.getAssignedFormModules(formSpec);
    for (AssignedFormModule module : modules) {
      module.getFormModule().beforeApproval(app);
    }

    makeDecision(sess.getPrincipal(), app, message, Decision.DecisionType.APPROVED); // TODO make sure to add transaction management to roll this back if anything fails down the line

    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    // user could have consolidated identities in perun in time between submission and approval
    Submission submission = submissionRepository.findById(app.getSubmissionId())
        .orElseThrow(() -> new DataInconsistencyException("Submission not found for application " + app.getId()));
    
    if (app.getIdmUserId() == null) {
      String perunUserId = idmService.getUserIdByIdentifier(submission.getIdentityIssuer(),
          submission.getIdentityIdentifier());
      app.setIdmUserId(perunUserId);
    }

    Application savedApp = applicationRepository.save(app);

    String idmUserId = propagateApprovalToIdm(savedApp);
    // consolidate all applications with the potentially newly created idmUserId
    consolidateSubmissions(idmUserId, submission.getIdentityIdentifier(), submission.getIdentityIssuer());

    for (AssignedFormModule module : modules) {
      module.getFormModule().onApproval(savedApp);
    }

    eventService.emitEvent(new ApplicationApprovedEvent(savedApp));
    return savedApp;
  }

  /**
   * Propagates the approval to the IdM, adds the applicant to the desired object. Returns ID of IdM user.
   * @param application
   * @return
   */
  private String propagateApprovalToIdm(Application application) {
    releaseLogins(application.getFormItemData());
    dropExistingLogins(application);

    String perunUserId = null;

    if (application.getType().equals(FormSpecification.FormType.INITIAL)) {
      if (application.getIdmUserId() == null) {
        perunUserId = idmService.createMemberForCandidate(application);
      } else {
        perunUserId = idmService.createMemberForUser(application);
      }
    } else if (application.getType().equals(FormSpecification.FormType.EXTENSION)) {
      perunUserId = idmService.extendMembership(application);
    }
    return perunUserId;
  }

  /**
   * Set newly retrieved attributes from application acceptance to existing open applications
   */
  private void consolidateSubmissions(String idmUserId, String identityIdentifier, String identityIssuer) {
    List<Submission> submissions = submissionRepository.findAllByIdentifierAndIssuer(identityIdentifier, identityIssuer);
    submissions.forEach(submission -> {
      submission.setSubmitterId(idmUserId);
      submission.getApplications().forEach(application -> application.setIdmUserId(idmUserId));
      applicationRepository.updateAll(submission.getApplications());
    });
    submissionRepository.updateAll(submissions);
  }

  @Override
  public Application rejectApplication(RegistrarAuthenticationToken sess, int applicationId, String message)  {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new EntityNotExistsException("Application", applicationId));

    makeDecision(sess.getPrincipal(), app, message, Decision.DecisionType.REJECTED);

    try {
      app.reject("Manually rejected by manager");
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    // TODO unreserve logins -> this probably also needs to be done in IdM/adapter (shortly before setting the attributes)

    // TODO how do we handle auto-submitted applications related to this one?

    Integer formSpecId = app.getFormSpecificationId();
    FormSpecification formSpec = formSpecificationRepository.findById(formSpecId).orElseThrow(() -> new EntityNotExistsException("FormSpecification", formSpecId));
    List<AssignedFormModule> modules = formService.getAssignedFormModules(formSpec);
    for (AssignedFormModule module : modules) {
      module.getFormModule().onRejection(app);
    }

    app = applicationRepository.save(app);
    eventService.emitEvent(new ApplicationRejectedEvent(app));

    return app;
  }

  @Override
  public Application requestChanges(RegistrarAuthenticationToken sess, int applicationId, String message) {
    Application app = applicationRepository.findById(applicationId)
                          .orElseThrow(() -> new EntityNotExistsException("Application", applicationId));

    if (StringUtils.isEmpty(message)) {
      // TODO move validation to controller
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
    decision.setApplicationId(app.getId());
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

    setSubmissionMetadata(submission);
    submission = submissionRepository.save(submission);

    List<Application> applications = new ArrayList<>();

    for (ApplicationForm appContext : submissionData.getPrefilledData()) {
      Application app = applyForMembership(appContext, submission, submissionData.getRedirectUrl());
      applications.add(app);
    }

    submission.setApplications(applications);

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
    FormSpecification formSpecification = formSpecificationRepository.findById(applicationForm.getFormSpecificationId()).orElseThrow(() -> new EntityNotExistsException("FormSpecification", applicationForm.getFormSpecificationId()));
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();

    if (applicationForm.getType() == FormSpecification.FormType.EXTENSION && !canExtendMembership(formSpecification.getGroupId())) {
      throw new IllegalArgumentException("User cannot extend membership in group: " + formSpecification.getGroupId());
    }

    if (checkOpenApplications(sess, formSpecification) != null && applicationForm.getType() != FormSpecification.FormType.UPDATE) {
      // user has applied for membership since loading the form
      throw new IllegalArgumentException("User already has an open application in group: " + formSpecification.getGroupId());
    }

    // TODO check that prerequisites are fulfilled?


    validateFilledFormData(applicationForm);
    normalizeFilledFormData(applicationForm);
    applicationForm.getFormItemData().forEach(item -> checkPrefilledValueConsistency(sess, item));
    reserveLogins(applicationForm.getFormItemData()); // TODO handle reserved logins if creating app object fails

    Application app = new Application(0, sess.getPrincipal().id(), formSpecification.getId(),
        applicationForm.getFormItemData(), redirectUrl, applicationForm.getType(), ApplicationState.PENDING,
        submission.getId());
    try {
      app.submit(formSpecification);
    } catch (InvalidApplicationStateTransitionException e) {
      // should not happen, we checked the items beforehand
      throw new DataInconsistencyException(e.getMessage());
    }

    app = applicationRepository.save(app);


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
    return applicationRepository.findById(id)
        .orElseThrow(() -> new EntityNotExistsException("Application", id));
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
    FormSpecification formSpec = formSpecificationRepository.findById(data.getFormSpecificationId()).orElseThrow(() -> new EntityNotExistsException("FormSpecification", data.getFormSpecificationId()));
    List<FormItem> items = formItemRepository.getFormItemsByFormIdAndType(formSpec.getId(), data.getType());

    Set<Integer> itemIds = items.stream().map(FormItem::getId).collect(Collectors.toSet());
    List<FormItemData> foreignItems = data.getFormItemData().stream()
                                          .filter((itemData) -> !itemIds.contains(itemData.getFormItem().getId()))
                                          .toList();
    if (!foreignItems.isEmpty()) {
      throw new DataInconsistencyException("Submitted form: " + formSpec.getId() + " contains data for items" +
                                               " not currently in that form: " + foreignItems);
    }

    List<ValidationError> result = items.stream()
                                       .map(item -> validate(item,
        data.getFormItemData().stream()
            .filter(itemData -> itemData.getFormItem().getId() == item.getId())
            .map(FormItemData::getValue)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null))
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

    Optional<String> prefilledValue = prefillStrategyResolver.prefill(itemData.getFormItem());
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
      FormSpecification.FormType type = selectFormType(new Requirement(autoSubmitTransition.getTargetFormSpecification().getGroupId(),
          autoSubmitTransition.getTargetFormState()));
      if (type == null) {
        return;
      }
      autoSubmitData = prepareApplicationForms(Map.of(autoSubmitTransition.getTargetFormSpecification(), type), null);
      applyForMemberships(autoSubmitData);
    } catch (Exception e) {
      // TODO log properly
      // consider adding error into result messages?
    }
  }

  @Override
  //@Transactional
  public void updateApplicationData(int applicationId, List<FormItemData> itemData) {
    Application application = applicationRepository.findById(applicationId).orElseThrow(() -> new EntityNotExistsException("Application", applicationId));

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
      
      // Get the ItemDefinition using the itemDefinitionId
      ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getFormItem().getItemDefinitionId())
          .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getFormItem().getItemDefinitionId()));
          
      if (!itemDefinition.isUpdatable()) {
        throw new IllegalArgumentException("Item " + item.getFormItem().getId() + " cannot be updated");
      }

      FormItemData existingItem = existingItemToDataMap.get(item.getFormItem());

      existingItem.setValue(item.getValue());

      if (itemDefinition.getType().isVerifiedItem()) {
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

  /**
   * Check whether all items of which values need to be assured, are assured
   * @param application
   * @return
   */
  private void attemptApplicationVerification(Application application) {
    List<FormItemData> unassuredItems = application.getFormItemData().stream()
        .filter(itemData -> {
          ItemDefinition itemDefinition = itemDefinitionRepository.findById(itemData.getFormItem().getItemDefinitionId())
              .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + itemData.getFormItem().getItemDefinitionId()));
          return itemDefinition.getType().isVerifiedItem();
        })
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

  //TODO should be enough to build the whole form page? or do we need more info?
  @Override
  public SubmissionContext loadForms(List<Requirement> requirements, String redirectUrl)
      throws IdmObjectNotExistsException {

    List<Requirement> prerequisites = new ArrayList<>();

    requirements.forEach(requirement -> prerequisites.addAll(getPrerequisiteRequirements(requirement, new ArrayList<>())));

    requirements.addAll(prerequisites);

    for (Requirement requirement : requirements) {
      if (!idmService.checkGroupExists(requirement.getGroupId())) {
        // TODO should not happen with message queue up, but alert admins/form managers?
        throw new IdmObjectNotExistsException("Group " + requirement.getGroupId() + " not found in underlying IDM",
            requirement.getGroupId());
      }
    }

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

      List<FormItemData> prefilledFormItemData = loadForm(sess, formSpecification, type);

      requiredForms.add(new ApplicationForm(formSpecification.getId(), prefilledFormItemData, type));
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

    formSpecification.setItems(formItemRepository.getFormItemsByFormIdAndType(formSpecification.getId(), type));

    List<FormItemData> prefilledFormItemData = prefillForm(sess, formSpecification);

    modules.forEach(module -> module.getFormModule().afterFormItemsPrefilled(sess.getPrincipal(), type, prefilledFormItemData));

    checkMissingPrefilledItems(sess, prefilledFormItemData);

    return prefilledFormItemData;
  }

  @Override
  public List<Identity> checkForSimilarIdentities(RegistrarAuthenticationToken sess, List<FormItemData> itemData) {
    return idmService.checkForSimilarUsers((String) sess.getCredentials(), itemData);
  }

  @Override
  public List<Identity> checkForSimilarIdentities(RegistrarAuthenticationToken sess) {
    return idmService.checkForSimilarUsers((String) sess.getCredentials());
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

  private ValidationError validate(FormItem item, String value) {
    // TODO ideally replace hardcoded strings with enums/inheritance and let GUI translate them
    // Get the ItemDefinition using the itemDefinitionId
    ItemDefinition itemDef = itemDefinitionRepository.findById(item.getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getItemDefinitionId()));

    if (itemDef.getType().isLayoutItem() && itemDef.isRequired()) {
      throw new IllegalStateException("Layout item required: " + this);
    }

    if (itemDef.getType().isLayoutItem() && value != null && !value.isEmpty()) {
      return new ValidationError(item.getId(), "Layout item " + itemDef.getTexts() + " cannot hold value");
    }

    if (itemDef.isRequired() && (value == null || value.isEmpty())) {
        return new ValidationError(item.getId(), "Field " + itemDef.getLabel() + " is required");
    }

    // TODO apply validators
    if (value != null && itemDef.getValidator() != null) {
        if (!value.matches(itemDef.getValidator())) {
            return new ValidationError(item.getId(), "Item " + itemDef.getLabel() + " must match constraint " + itemDef.getValidator());
        }
    }

    return null;
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
          // Get the ItemDefinition using the itemDefinitionId
          ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getFormItem().getItemDefinitionId())
              .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getFormItem().getItemDefinitionId()));
              
          if (itemDefinition.getPrefillStrategyIds() == null ||
              itemDefinition.getPrefillStrategyIds().isEmpty()) {
            unmodifiableRequiredButEmpty.add(item);
          } else {
            itemsWithMissingData.add(item);
          }
        });
    if (!unmodifiableRequiredButEmpty.isEmpty()) {
      throw new RuntimeException("Items that are hidden/disabled but do not have a source attribute should not exist:" +
                                     unmodifiableRequiredButEmpty);
    }
    if (!itemsWithMissingData.isEmpty()) {
//      if (sess.getPrincipal().id() != null) {
//        // user exists in underlying IdM , hence source attribute values should exist?
//        // TODO this does not necessarily apply to all prefill strategies
//        throw new RuntimeException("Could not prefill the following disabled/hidden attributes: " + itemsWithMissingData);
//      }
    }
  }

  /**
   * Checks whether there is an issue with the item's visibility/editable settings.
   * @param item
   * @param prefilledFormItemData
   * @return
   */
  private boolean hasItemIncorrectVisibility(FormItemData item, List<FormItemData> prefilledFormItemData) {
    // Get the ItemDefinition using the itemDefinitionId
    ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getFormItem().getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getFormItem().getItemDefinitionId()));
        
    return itemDefinition.isRequired() && StringUtils.isEmpty(item.getPrefilledValue()) &&
            (isItemHidden(item, prefilledFormItemData) || isItemDisabled(item, prefilledFormItemData));
  }

  /**
   * Checks whether the item will be hidden in the form
   * @param item
   * @param prefilledFormItemData
   * @return
   */
  private boolean isItemHidden(FormItemData item, List<FormItemData> prefilledFormItemData) {
    // Get the ItemDefinition using the itemDefinitionId
    ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getFormItem().getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getFormItem().getItemDefinitionId()));
        
    return isItemConditionApplied(item.getPrefilledValue(), prefilledFormItemData,
        itemDefinition.getHidden(),
        item.getFormItem().getHiddenDependencyItemId());
  }

  /**
   * Checks whether the item will be disabled in the form
   * @param item
   * @param prefilledFormItemData
   * @return
   */
  private boolean isItemDisabled(FormItemData item, List<FormItemData> prefilledFormItemData) {
    // Get the ItemDefinition using the itemDefinitionId
    ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getFormItem().getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getFormItem().getItemDefinitionId()));
        
    return isItemConditionApplied(item.getPrefilledValue(), prefilledFormItemData,
        itemDefinition.getDisabled(),
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
                                         ItemDefinition.Condition condition, Integer dependencyItemId) {
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

    // Get the ItemDefinition using the itemDefinitionId
    ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getItemDefinitionId()));

    if (!sess.isAuthenticated()) {
      // TODO potentially move this to the strategy logic? Could there be a strategy that returns value for unauthenticated users?
      return itemDefinition.getDefaultValue();
    }

    Optional<String> prefilledValue = prefillStrategyResolver.prefill(item);

    return prefilledValue.orElse(itemDefinition.getDefaultValue());

  }

  /**
   * Checks user attributes for existing login values, unreserve password if they do exist. Set login attributes if not.
   * TODO the reason for a separate assignment of login attribute values is to not overwrite existing ones. This can be
   *  solved by checking existing logins before assigning all attributes, filtering those items out (while unreserving passwords),
   *  and bulk assigning all items at once. This is easily done when we know user exists in perun (createMemberForUser/extendMembership calls)
   *  A potentially problematic case is `createMemberForCandidate` (at least that's what old registrar says), because it
   *  can join identities during the call. But all the method does is search using `getUserByExtSourceNameAndExtLogin`,
   *  which is what we do in `approveApplication` earlier - e.g. we should be able to tell (almost) for sure whether logins
   *  exist or not
   * @param application
   */
  private void dropExistingLogins(Application application) {
    List<FormItemData> loginItemsToLeaveOut = new ArrayList<>();
    if (application.getIdmUserId() != null) {
      application.getFormItemData().stream()
          .filter(item -> {
            // Get the ItemDefinition using the itemDefinitionId
            ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getFormItem().getItemDefinitionId())
                .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getFormItem().getItemDefinitionId()));
            return itemDefinition.getType().equals(ItemType.LOGIN);
          })
          .forEach(loginItem -> {
            // Get the ItemDefinition using the itemDefinitionId
            ItemDefinition itemDefinition = itemDefinitionRepository.findById(loginItem.getFormItem().getItemDefinitionId())
                .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + loginItem.getFormItem().getItemDefinitionId()));
                
            // Get the Destination using the destinationId
           Destination destination = destinationRepository.findById(itemDefinition.getDestinationId())
                .orElseThrow(() -> new IllegalStateException("Destination not found for id: " + itemDefinition.getDestinationId()));
                
            String perunLogin;
            try {
              // TODO overall the reserved logins logic might be too `perun-specific`
              FormSpecification formSpec = formSpecificationRepository.findById(application.getFormSpecificationId()).orElseThrow(() -> new EntityNotExistsException("FormSpecification", application.getFormSpecificationId()));
              perunLogin = idmService.getAttribute(destination.getUrn(),
                  application.getIdmUserId(), formSpec.getGroupId(),
                  formSpec.getVoId());
            } catch (IdmAttributeNotExistsException e) {
              // TODO login attribute definition was removed in IdM between submission and approval, alert admins?
              throw new IllegalStateException("Login item attribute has been deleted in the underlying IdM system, " +
                                                 e.getMessage());
            }
            String itemLogin = loginItem.getValue();
            String namespace = extractNamespaceFromLoginAttrName(destination.getUrn());
            if (!StringUtils.isEmpty(perunLogin)) {
              loginItemsToLeaveOut.add(loginItem);
              idmService.deletePassword(namespace, itemLogin);
            }
          });
      application.getFormItemData().removeAll(loginItemsToLeaveOut);
    }
  }

  private void releaseLogins(List<FormItemData> itemData) {
    itemData.stream()
        .filter(item -> {
          // Get the ItemDefinition using the itemDefinitionId
          ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getFormItem().getItemDefinitionId())
              .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getFormItem().getItemDefinitionId()));
          return itemDefinition.getType().equals(ItemType.LOGIN);
        })
        .forEach(loginItem -> {
          // Get the ItemDefinition using the itemDefinitionId
          ItemDefinition itemDefinition = itemDefinitionRepository.findById(loginItem.getFormItem().getItemDefinitionId())
              .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + loginItem.getFormItem().getItemDefinitionId()));
              
          // Get the Destination using the destinationId
          Destination destination = destinationRepository.findById(itemDefinition.getDestinationId())
              .orElseThrow(() -> new IllegalStateException("Destination not found for id: " + itemDefinition.getDestinationId()));
              
          String namespace = extractNamespaceFromLoginAttrName(destination.getUrn());
          if (namespace == null) {
            throw new IllegalStateException("Destination attribute of " + loginItem + " is not a login-namespace attribute.");
          }
          String login =  loginItem.getValue();
          idmService.releaseLogin(namespace, login);
        });
  }

  /**
   * Reserves the login in the set namespace
   * TODO this has to be implemented using IdM calls -> how do we ensure that IdM does not create a user with the reserved
   *  login (e.g. using service accounts).
   * @param itemData
   */
  private void reserveLogins(List<FormItemData> itemData) {

    for (FormItemData formItemData : itemData) {
      // Get the ItemDefinition using the itemDefinitionId
      ItemDefinition itemDefinition = itemDefinitionRepository.findById(formItemData.getFormItem().getItemDefinitionId())
          .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + formItemData.getFormItem().getItemDefinitionId()));
          
      if (itemDefinition.getType().equals(ItemType.LOGIN)) {
        if (StringUtils.isEmpty(formItemData.getValue()) ||
                formItemData.isValueAssured()) { // login with prefilled value => value alr reserved
          continue;
        }
        // could be a lot of calls, at the same time this checks that the attribute actually exists
        // AttributeDefinition attrDef = idmService.getAttributeDefinition(formItemData.getFormItem().getDestinationIdmAttribute());
        
        // Get the Destination using the destinationId
        Destination destination = destinationRepository.findById(itemDefinition.getDestinationId())
            .orElseThrow(() -> new IllegalStateException("Destination not found for id: " + itemDefinition.getDestinationId()));
            
        String namespace = extractNamespaceFromLoginAttrName(destination.getUrn());
        if (namespace == null) {
          throw new IllegalStateException("Destination attribute of " + formItemData + " is not a login-namespace attribute.");
        }
        String login =  formItemData.getValue();
        if (idmService.isLoginAvailable(namespace, login)) {
          idmService.reserveLogin(namespace, login);
          itemData.stream()
              .filter(passwordItem -> {
                // Get the ItemDefinition using the itemDefinitionId
                ItemDefinition passwordItemDefinition = itemDefinitionRepository.findById(passwordItem.getFormItem().getItemDefinitionId())
                    .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + passwordItem.getFormItem().getItemDefinitionId()));
                return passwordItemDefinition.getType().equals(ItemType.PASSWORD);
              })
              .forEach(passwordItem -> idmService.reservePassword(namespace, login, passwordItem.getValue()));

        } else {
          // TODO unified ApplicationNotCreated event once we have custom checked exceptions and can collect them to notify administrators
          throw new RuntimeException("Login " + formItemData.getValue() + " is blocked");
        }
      }
    }
  }

  private String extractNamespaceFromLoginAttrName(String loginAttrName) {
    if (loginAttrName == null || !loginAttrName.contains("login-namespace")) {
      return null; // Not a login-namespace attribute
    }

    String[] parts = loginAttrName.split("login-namespace:");

    // If there is a namespace part after "login-namespace:"
    if (parts.length > 1 && !parts[1].isEmpty()) {
      return parts[1]; // return e.g. "admin-meta"
    }

    // If there’s nothing after "login-namespace:", it has no namespace
    return null;
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
          .filter(app -> {
            if (app.getIdmUserId() != null && Objects.equals(app.getIdmUserId(), sess.getPrincipal().id())) {
              return true;
            }
            // Load submission to check identity
            Submission submission = submissionRepository.findById(app.getSubmissionId())
                .orElseThrow(() -> new DataInconsistencyException("Submission not found for application " + app.getId()));
            return Objects.equals(submission.getIdentityIssuer(), sess.getPrincipal().attribute(ISSUER_CLAIM)) &&
                submission.getIdentityIdentifier().equals(sess.getPrincipal().attribute(IDENTIFIER_CLAIM));
          })
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
  private boolean checkUserMembership(String groupId) {
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
  private boolean canExtendMembership(String groupId) {
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
      FormSpecification formSpecification = formSpecificationRepository.findByGroupId(requirement.getGroupId())
                      .orElseThrow(() -> new EntityNotExistsException("FormSpecification", -1, "Form for group " + requirement.getGroupId() + " not found"));
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
    FormSpecification formSpecification = formSpecificationRepository.findByGroupId(requirement.getGroupId())
                      .orElseThrow(() -> new EntityNotExistsException("FormSpecification", -1, "Form for group " + requirement.getGroupId() + " not found"));

    List<FormTransition> prerequisiteTransitions = formService.getPrerequisiteTransitions(formSpecification,
        requirement.getTargetState());

    prerequisiteTransitions.forEach(transition -> {
      Requirement prerequisiteRequirement = new Requirement(transition.getTargetFormSpecification().getGroupId(),
          transition.getTargetFormState());
      requirements.add(prerequisiteRequirement);
      requirements.addAll(getPrerequisiteRequirements(prerequisiteRequirement, requirements));
    });

    return requirements;
  }
}
