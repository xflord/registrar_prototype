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
import org.perun.registrarprototype.events.ApplicationRejectedEvent;
import org.perun.registrarprototype.events.ApplicationSubmittedEvent;
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
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.ApplicationContext;
import org.perun.registrarprototype.models.Identity;
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
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl {
  public static final String IDENTIFIER_CLAIM = "sub";
  public static final String ISSUER_CLAIM = "iss";
  private final ApplicationRepository applicationRepository;
  private final FormRepository formRepository;
  private final SubmissionRepository submissionRepository;
  private final DecisionRepository decisionRepository;
  private final EventService eventService;
  private final AuthorizationService authorizationService;
  private final FormServiceImpl formService;
  private final IdMService idmService;
  private final SessionProvider sessionProvider;

  public ApplicationServiceImpl(ApplicationRepository applicationRepository, FormRepository formRepository,
                                SubmissionRepository submissionRepository, DecisionRepository decisionRepository,
                                EventService eventService,
                                AuthorizationService authorizationService, FormServiceImpl formService,
                                IdMService idmService, SessionProvider sessionProvider) {
    this.applicationRepository = applicationRepository;
    this.formRepository = formRepository;
    this.submissionRepository = submissionRepository;
    this.decisionRepository = decisionRepository;
    this.eventService = eventService;
    this.authorizationService = authorizationService;
    this.formService = formService;
    this.idmService = idmService;
    this.sessionProvider = sessionProvider;
  }

  // TODO modify this to call all the new methods to test the flow
//  public Application registerUserToGroup(CurrentUser sess, int groupId, List<FormItemData> itemData)
//      throws InvalidApplicationDataException {
//    Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
//    Application app = this.applyForMembership(new ApplicationContext(form, groupId, itemData, Form.FormType.INITIAL));
//
//    try {
//      app.approve();
//    } catch (InvalidApplicationStateTransitionException e) {
//      throw new RuntimeException(e);
//    }
//    app = applicationRepository.save(app);
//    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), Integer.parseInt(sess.name()), groupId));
//
//    return app;
//  }

  public Application approveApplication(RegistrarAuthenticationToken sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(app.getFormId());

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    for (AssignedFormModule module : modules) {
      module.getFormModule().beforeApproval(app);
    }

    if (!authorizationService.canDecide(sess, form.getGroupId())) {
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

    Integer idmUserId = propagateApprovalToIdm(app, form.getGroupId());
    // consolidate all applications with the potentially newly created idmUserId
    consolidateSubmissions(idmUserId, app.getSubmission().getIdentityIdentifier(), app.getSubmission().getIdentityIssuer());

    for (AssignedFormModule module : modules) {
      module.getFormModule().onApproval(app);
    }

    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), app.getIdmUserId(), form.getGroupId()));
    return app;
  }

  /**
   * Propagates the approval to the IdM, adds the applicant to the desired object. Returns ID of IdM user.
   * @param application
   * @param groupId
   * @return
   */
  private Integer propagateApprovalToIdm(Application application, Integer groupId) {
    // TODO unreserve logins

    if (application.getType().equals(Form.FormType.INITIAL)) {
      if (application.getIdmUserId() == null) {
        return idmService.createMemberForCandidate(application, groupId);
      }
      return idmService.createMemberForUser(application, groupId);
    } else if (application.getType().equals(Form.FormType.EXTENSION)) {
      return idmService.extendMembership(application, groupId);
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

  public Application rejectApplication(RegistrarAuthenticationToken sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(app.getFormId());

    if (!authorizationService.canDecide(sess, form.getGroupId())) {
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

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    for (AssignedFormModule module : modules) {
      module.getFormModule().onRejection(app);
    }

    app = applicationRepository.save(app);
    eventService.emitEvent(new ApplicationRejectedEvent(app.getId(), app.getIdmUserId(), form.getGroupId()));

    return app;
  }

  public Application requestChanges(RegistrarAuthenticationToken sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(app.getFormId());

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
    eventService.emitEvent(new ChangesRequestedToApplicationEvent(app.getId(), app.getIdmUserId(), form.getGroupId()));

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
    return decisionRepository.save(decision);
  }

  /**
   * Submits an application to each of the supplied forms, aggregates them under one submission object along with
   * information about the submitter, automatically submits applications (to forms that are marked as such), retrieves
   * forms to which to redirect, and displays result messages.
   * @param submissionData
   * @return
   */
  public SubmissionResult applyForMemberships(SubmissionContext submissionData) {
    Submission submission = new Submission();
    List<Application> applications = new ArrayList<>();

    for (ApplicationContext appContext : submissionData.getPrefilledData()) {
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

  /**
   * Validates the input data for the form, marks assured prefilled items, and submits an application.
   * @param applicationContext
   * @return
   */
  public Application applyForMembership(ApplicationContext applicationContext, Submission submission, String redirectUrl) {
    Form form = formRepository.findById(applicationContext.getForm().getId()).orElseThrow(() -> new IllegalArgumentException("Form not found"));
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();

    if (applicationContext.getType() == Form.FormType.EXTENSION && !canExtendMembership(applicationContext.getGroupId())) {
      throw new IllegalArgumentException("User cannot extend membership in group: " + applicationContext.getGroupId());
    }

    if (checkOpenApplications(sess, form) != null && applicationContext.getType() != Form.FormType.UPDATE) {
      // user has applied for membership since loading the form
      throw new IllegalArgumentException("User already has an open application in group: " + applicationContext.getGroupId());
    }

    Map<String, String> reservedPrincipalLogins = getReservedLoginsForPrincipal(sess.getPrincipal()); // call here to avoid unnecessary idm calls

    validateFilledFormData(applicationContext);
    applicationContext.getPrefilledItems().forEach(item -> checkPrefilledValueConsistency(sess, item, reservedPrincipalLogins));

    Application app = new Application(0, Integer.parseInt(sess.getPrincipal().name()), form.getId(),
        applicationContext.getPrefilledItems(), null, applicationContext.getType());
    app.setRedirectUrl(redirectUrl);
    app.setSubmission(submission);
    try {
      app.submit(form);
    } catch (InvalidApplicationStateTransitionException e) {
      // should not happen, we checked the items beforehand
      throw new DataInconsistencyException(e.getMessage());
    }

    app = applicationRepository.save(app);

    reserveLogins(applicationContext.getPrefilledItems());

    // TODO if we emit events asynchronously this might be problematic (not sure rollback would work here)
    eventService.emitEvent(new ApplicationSubmittedEvent(app.getId(), Integer.parseInt(sess.getPrincipal().name()), applicationContext.getGroupId()));

    return app;
  }

  /**
   * Validates that the submitted form data is correctly filled in (e.g. required items not empty, values match
   * constraints, etc.)
   * @param data
   */
  private void validateFilledFormData(ApplicationContext data) {
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
   * Checks that form data is validated against form item constraints, checks that prefilled data is still valid (e.g.
   * the submitted value matches prefilled value from source attributes), if so set a flag to indicate that the form item
   * data has been validated (LOA or just a flag).
   * TODO do we want the option to require item value to be validated? This can optionally be done using `beforeApproval` module hook
   * @param sess
   * @param itemData
   */
  private void checkPrefilledValueConsistency(RegistrarAuthenticationToken sess, FormItemData itemData, Map<String, String> reservedLogins) {
    itemData.setValueAssured(false);
    if (StringUtils.isEmpty(itemData.getValue())) {
      return;
    }

    if (!sess.isAuthenticated()) {
      return;
    }

    if (itemData.getFormItem().getType().equals(FormItem.Type.LOGIN)) {
      String loginValue = tryToFillLoginItem(itemData.getFormItem(), reservedLogins);
      if (!StringUtils.isEmpty(loginValue) && loginValue.equals(itemData.getValue())) {
        itemData.setValueAssured(true);
        return;
      }
    }

    // TODO this is where we in the future want to handle attribute freshness/provenance logic

    String identityValue = getIdentityAttributeValue(sess.getPrincipal(), itemData.getFormItem());
    itemData.setIdentityAttributeValue(identityValue);
    String idmValue = getIdmAttributeValue(sess.getPrincipal(), itemData.getFormItem());
    itemData.setIdmAttributeValue(idmValue);

    if (Objects.equals(idmValue, itemData.getValue()) || Objects.equals(identityValue, itemData.getValue())) {
      itemData.setValueAssured(true);
    }
  }

  /**
   * Perform auto-submission of a form marked for auto submit. Use data from principal and the previous submission to
   * fill out form item data.
   * TODO run this asynchronously?
   * @param form
   * @param submissionData
   */
  public void autoSubmitForm(Form form, SubmissionContext submissionData) {
    SubmissionContext autoSubmitData;
    try {
      autoSubmitData = assemblePrefilledForms(List.of(form), null);
      applyForMemberships(autoSubmitData);
    } catch (Exception e) {
      System.out.println("Error while auto-submitting application for form " + form.getId() + " with error: " + e.getMessage()); // TODO log properly
      // consider adding error into result messages?
    }
  }

  /**
   * Auto-fill items of an automatically submitted application using the data of the set of submitted applications
   * that triggered the auto-submission.
   * TODO do we match based on identical/similar form item names, define an exact mapping between forms (e.g. form item id to form item id)
   *   or just fill items with same source/destination attribute names + metadata from principal?
   *   Prefill using the classic submission flow (in case the auto form has more prefilled items) / use just data from the previous submission (fewer IdM calls)?
   *   What if user rewrote prefilled values in the application?
   * @param sess
   * @param form
   * @param prefilledItems
   */
  private ApplicationContext prefillAutosubmitFormItems(CurrentUser sess, Form form, List<FormItemData> prefilledItems) {
    return null;
  }

  /**
   * Tries to load prefilled data for redirect forms. If this fails for whatever reason, log the error and (potentially)
   * return it via the submission result messages.
   * @param sess
   * @param redirectForms
   * @return
   */
  private void setRedirectForms(CurrentUser sess, List<Form> redirectForms, SubmissionResult submissionResult) {
    if (!redirectForms.isEmpty()) {
      // TODO do we want to load prerequisites of redirect forms like so?
      List<Integer> groupIds = redirectForms.stream().map(Form::getGroupId).distinct().toList();
      try {
        submissionResult.setRedirectForms(loadForms(groupIds, submissionResult.getRedirectUrl(), false));
        // theoretically just the set of ids (or whatever the API will take as the entrypoint of registration flow) is enough and just redirect user to that endpoint
      } catch (Exception e) {
        System.out.println("Error assembling redirect forms: " + e.getMessage()); // TODO log properly
        // probably best to use some constant here so that it can be localized
        // submissionResult.addMessage("Error assembling redirect forms: " + e.getMessage());
        submissionResult.addMessage("Could not redirect to another form. Please contact support.");
      }
    }
  }

  /**
   * Entry from GUI -> for a given group, determine which forms to display to the user, prefill form items, and return to GUI
   *
   *
   * @param groupIds set of groups the user wants to apply for membership in
   * @param checkSimilarUsers Call this with FALSE only from GUI after user decided on consolidation
   * @return prefilled submission object, with the redirect URL and individual prefilled form data (prefilled items, form type) TODO should be enough to build the whole form page? or do we need more info?
   */
  public SubmissionContext loadForms(List<Integer> groupIds, String redirectUrl, boolean checkSimilarUsers) {
    if (checkSimilarUsers) { // potentially enable/disable with config
      // Check this once when loading registrar for the first time -> offer user the option to consolidate, do not check again afterwards
      List<Identity> similarIdentities =
          idmService.checkForSimilarUsers((String) sessionProvider.getCurrentSession().getCredentials());
      if (!similarIdentities.isEmpty()) {
        // handle this in gui to display message and offer consolidator redirect
        throw new SimilarIdentitiesFoundException("Found similar identities: " + similarIdentities);
      }
    }
    List<Form> requiredForms = determineGroupSet(groupIds);
    return assemblePrefilledForms(requiredForms, redirectUrl);
  }

  /**
   * Determines the application types for each form, retrieves and prefills items for that form type, and returns this
   * aggregated data.
   * @param requiredForms
   * @param redirectUrl
   * @return
   */
  private SubmissionContext assemblePrefilledForms(List<Form> requiredForms, String redirectUrl) {
    List<ApplicationContext> applicationFormData = new ArrayList<>();
    RegistrarAuthenticationToken sess = sessionProvider.getCurrentSession();
    for (Form form : requiredForms) {
      Application openApp = checkOpenApplications(sess, form);
      if (openApp != null) {
        if (openApp.getState().equals(ApplicationState.CHANGES_REQUESTED)) {
          // TODO we want to indicate that the application exists, but there are changes requested -> display it
          continue;
        }
        // TODO here we want to indicate that the application exists, just hasn't been accepted yet -> no need to display it?
        continue;
      }

      Form.FormType type = selectFormType(form);
      if (type == null) {
        continue;
      }

//      // now that we have form type, retrieve prerequisite forms
//      formService.getPrerequisiteTransitions(form, type).forEach(transition -> {
//        if (checkOpenApplications(sess, form) == null) {
//          Form.FormType targetFormType = selectFormType(sess, transition.getTargetForm());
//          if (transition.getTargetFormTypes().contains(targetFormType) && targetFormType != null) {
//            Form prerequisiteForm =  transition.getTargetForm();
//            prerequisiteForm.setItems(formService.getFormItems(sess, form, targetFormType));
//            prefilledFormData.add(new PrefilledFormData(prerequisiteForm, prerequisiteForm.getGroupId(),
//                loadForm(sess, prerequisiteForm, targetFormType), targetFormType));
//          }
//        }
//      });

      form.setItems(formService.getFormItems(form, type));

      List<FormItemData> prefilledFormItemData = loadForm(sess, form, type);

      applicationFormData.add(new ApplicationContext(form, form.getGroupId(), prefilledFormItemData,
          type));
    }

    // TODO probably some logic to order the individual forms? (e.g. prerequisites before their source form - or do we want
    //  to display prerequisites independently and have them redirect to the original forms?
    return new SubmissionContext(redirectUrl, applicationFormData);
  }

  /**
   * For the group associated with the given form, determine the FormType (e.g. INITIAL, EXTENSION)
   * @param form
   * @return
   */
  private Form.FormType selectFormType(Form form) {

    if (checkUserMembership(form.getGroupId())) {
      if (canExtendMembership(form.getGroupId())) {
        return Form.FormType.EXTENSION;
      }
      // todo display error message if no forms returned?
      return null;
    }

    return Form.FormType.INITIAL;
  }

  /**
   * Generates prefilled form item data for the form and its type, calls module hooks, and ensures the validity of form
   * item visibility.
   * @param sess
   * @param form
   * @param type
   * @return
   */
  public List<FormItemData> loadForm(RegistrarAuthenticationToken sess, Form form, Form.FormType type) {
//    if (type.equals(Form.FormType.UPDATE)) {
//      // TODO prefill form with data from already submitted app if loading modifying/update form type
//    }

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    modules.forEach(module -> module.getFormModule().canBeSubmitted(sess.getPrincipal(), type, module.getOptions()));

    List<FormItemData> prefilledFormItemData = prefillForm(sess, form);

    modules.forEach(module -> module.getFormModule().afterFormItemsPrefilled(sess.getPrincipal(), type, prefilledFormItemData));

    checkMissingPrefilledItems(sess, prefilledFormItemData);

    return prefilledFormItemData;
  }

  /**
   * To be called from GUI in case the user modifies items that could provide us with information to connect identities
   * with (e.g. email address, display name)
   * @param itemData
   * @return
   */
  public List<Identity> checkForSimilarIdentities(List<FormItemData> itemData) {
    return idmService.checkForSimilarUsers(itemData);
  }

  /**
   * Returns prefilled form item data for supplied form's items.
   * Allow admin to define where to fill item from (e.g. federation, IdM, more complex solutions/external sources -> from submitted prerequisites)]
   * TODO we can also hide prerequisite submitted apps behind external sources (same with redirect forms)
   * @param sess
   * @param form Form object with set form items array
   * @return
   */
  private List<FormItemData> prefillForm(RegistrarAuthenticationToken sess, Form form) {
    Map<String, String> reservedPrincipalLogins = getReservedLoginsForPrincipal(sess.getPrincipal()); // call here to avoid unnecessary idm calls

    return form.getItems().stream()
        .map((item) -> new FormItemData(item, null,
            calculatePrefilledValue(sess, item, reservedPrincipalLogins)))
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
          if (StringUtils.isEmpty(item.getFormItem().getSourceIdentityAttribute()) &&
              StringUtils.isEmpty(item.getFormItem().getSourceIdmAttribute())) {
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
  private String calculatePrefilledValue(RegistrarAuthenticationToken sess, FormItem item, Map<String, String> reservedLogins) { // again decide whether to pass principal as argument or retrieve it from the current session

    if (!sess.isAuthenticated()) {
      return item.getDefaultValue();
    }

    if (item.getType().equals(FormItem.Type.LOGIN)) {
     String login = tryToFillLoginItem(item, reservedLogins);
     if (!StringUtils.isEmpty(login)) {
       return login;
     }
   }

    String identityValue = getIdentityAttributeValue(sess.getPrincipal(), item);
    if (item.isPreferIdentityAttribute() && identityValue != null) {
      return identityValue;
    }
    String idmValue = getIdmAttributeValue(sess.getPrincipal(), item);
    if (idmValue != null) {
      return idmValue;
    } else if (identityValue != null) {
      return identityValue;
    }
    return item.getDefaultValue();
  }

  /**
   * Retrieves value of item's source identity attribute
   * @param principal
   * @param item
   * @return
   */
  private String getIdentityAttributeValue(CurrentUser principal, FormItem item) {
    String sourceAttr = item.getSourceIdentityAttribute();
    return sourceAttr == null ? null : principal.attribute(sourceAttr);
  }

  /**
   * Retrieves value of item's source IdM attribute.
   * @param principal
   * @param item
   * @return
   */
  private String getIdmAttributeValue(CurrentUser principal, FormItem item) {
    Form form = formRepository.findById(item.getFormId()).orElseThrow(() -> new DataInconsistencyException("Form with ID " + item.getFormId() + " not found for form item " + item.getId()));

    String sourceAttr = item.getSourceIdmAttribute();
    if (sourceAttr == null) {
      return null;
    }
    if (sourceAttr.startsWith(idmService.getUserAttributeUrn())) {
      return idmService.getUserAttribute(principal.id(), sourceAttr);
    } else if (sourceAttr.startsWith(idmService.getVoAttributeUrn())) {
      return idmService.getVoAttribute(sourceAttr, form.getVoId());
    } else if (sourceAttr.startsWith(idmService.getMemberAttributeUrn()) && form.getGroupId() > 0) { // TODO can get member attr just from voId
      return idmService.getMemberAttribute(principal.id(), sourceAttr, form.getGroupId());
    } else if (sourceAttr.startsWith(idmService.getGroupAttributeUrn()) && form.getGroupId() > 0) { // TODO better check if group is present
      return idmService.getGroupAttribute(sourceAttr, form.getGroupId());
    } else {
      throw new IllegalArgumentException("Unsupported attribute source: " + sourceAttr);
    }
  }

  /**
   * Tries to fill items with the destination attribute matching the LOGIN urn
   * @param item
   * @param reservedLogins
   * @return
   */
  private String tryToFillLoginItem( FormItem item, Map<String, String> reservedLogins) {
    for (String namespace : reservedLogins.keySet()) {
      String loginAttributeDefinition = idmService.getLoginAttributeUrn() + namespace;
      if (item.getDestinationIdmAttribute().equals(loginAttributeDefinition)) {
        return reservedLogins.get(namespace);
      }
    }
    return null;
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
   * Returns all logins reserved by the authenticated principal
   * @param principal
   * @return a map of reserved logins, the keys being namespaces of the logins
   */
  private Map<String, String> getReservedLoginsForPrincipal(CurrentUser principal) {
    if (principal == null) {
      return new HashMap<>();
    }
    return new HashMap<>();
  }


  /**
   * Check whether user already has open application in the given group/VO (for the given form). If so, use this information
   * to determine FormType (e.g., if user has open application, update this application -  TODO do we use separate application type or just update the data?)
   * TODO also check by data stored in the Submission object
   * @param form
   */
  private Application checkOpenApplications(RegistrarAuthenticationToken sess, Form form) {
    if (sess.isAuthenticated()) {
      // TODO we need to have identities consolidated before this point
      Optional<Application> foundApp = applicationRepository.findByFormId(form.getId()).stream()
          .filter(app -> app.getState().isOpenState())
          .filter(app -> (app.getIdmUserId() != null && app.getIdmUserId() == sess.getPrincipal().id()) ||
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
      return idmService.getGroupIdsWhereUserIsMember(sess.getPrincipal().id()).contains(groupId);
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
   * @param groupIds TODO not sure what the input parameters will be
   * @return list of group ids to submit applications to
   */
  private List<Form> determineGroupSet(List<Integer> groupIds) {
    List<Form> requiredForms = new ArrayList<>();
    for (Integer groupId : groupIds) {
      Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form for group " + groupId + " not found"));
      // TODO do we always display EXTENSION forms if user is already member of some groups?

      requiredForms.add(form);
    }
    return requiredForms;
  }
}
