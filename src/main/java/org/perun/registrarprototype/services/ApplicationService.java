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
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.PrefilledFormData;
import org.perun.registrarprototype.models.PrefilledSubmissionData;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.models.ValidationError;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.DecisionRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.SubmissionRepository;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
  private final ApplicationRepository applicationRepository;
  private final FormRepository formRepository;
  private final SubmissionRepository submissionRepository;
  private final DecisionRepository decisionRepository;
  private final EventService eventService;
  private final PerunIntegrationService perunIntegrationService;
  private final AuthorizationService authorizationService;
  private final FormService formService;
  private final IdMService idmService;

  public ApplicationService(ApplicationRepository applicationRepository, FormRepository formRepository,
                            SubmissionRepository submissionRepository, DecisionRepository decisionRepository,
                            EventService eventService, PerunIntegrationService perunIntegrationService,
                            AuthorizationService authorizationService, FormService formService,
                            IdMService idmService) {
    this.applicationRepository = applicationRepository;
    this.formRepository = formRepository;
    this.submissionRepository = submissionRepository;
    this.decisionRepository = decisionRepository;
    this.eventService = eventService;
    this.perunIntegrationService = perunIntegrationService;
    this.authorizationService = authorizationService;
    this.formService = formService;
    this.idmService = idmService;
  }

  // TODO modify this to call all the new methods to test the flow
  public Application registerUserToGroup(CurrentUser sess, int groupId, List<FormItemData> itemData)
      throws InvalidApplicationDataException {
    Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
    Application app = this.applyForMembership(sess, new PrefilledFormData(form, groupId, itemData, Form.FormType.INITIAL));

    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }
    app = applicationRepository.save(app);
    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), Integer.parseInt(sess.id()), groupId));

    perunIntegrationService.registerUserToGroup(Integer.parseInt(sess.id()), groupId);

    return app;
  }

  public Application approveApplication(CurrentUser sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(sess, app.getFormId());

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    for (AssignedFormModule module : modules) {
      module.getFormModule().beforeApproval(app);
    }

    if (!authorizationService.isAuthorized(sess, form.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to approve this application");
    }

    makeDecision(sess, app, message, Decision.DecisionType.APPROVED);

    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    // TODO unreserve logins

    app = applicationRepository.save(app);

    for (AssignedFormModule module : modules) {
      // TODO if this was the first approved initial app (e.g., user ID from IdM was not available before but is now)
      //  update all other submissions matching this user with IdM ID (and possibly other attributes)
      module.getFormModule().onApproval(app);
    }
    // TODO directly call IdM (this could be done via module) / call adapter (again this could be done via modules) / or possibly emit event with whole app object + submission data
    perunIntegrationService.registerUserToGroup(app.getUserId(), form.getGroupId());

    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), app.getUserId(), form.getGroupId()));
    return app;
  }

  public Application rejectApplication(CurrentUser sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(sess, app.getFormId());

    if (!authorizationService.isAuthorized(sess, form.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to reject this application");
    }

    makeDecision(sess, app, message, Decision.DecisionType.REJECTED);

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
    eventService.emitEvent(new ApplicationRejectedEvent(app.getId(), app.getUserId(), form.getGroupId()));

    return app;
  }

  public Application requestChanges(CurrentUser sess, int applicationId, String message) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(sess, app.getFormId());

    if (StringUtils.isEmpty(message)) {
      throw new IllegalArgumentException("Cannot request changes without a message");
    }

    if (!authorizationService.isAuthorized(sess, form.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to request changes to this application");
    }

    makeDecision(sess, app, message, Decision.DecisionType.CHANGES_REQUESTED);

    try {
      app.requestChanges();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    app = applicationRepository.save(app);
    eventService.emitEvent(new ChangesRequestedToApplicationEvent(app.getId(), app.getUserId(), form.getGroupId()));

    return app;
  }

  /**
   * Creates the decision object filled with metadata
   * @param sess
   * @param app
   * @param message
   * @param decisionType
   * @return
   */
  private Decision makeDecision(CurrentUser sess, Application app, String message, Decision.DecisionType decisionType) {
    Decision decision = new Decision();
    decision.setApplication(app);
    decision.setApproverId(sess.id());
    decision.setApproverName(sess.attribute("name"));
    decision.setDecisionType(decisionType);
    decision.setMessage(message);
    return decisionRepository.save(decision);
  }

  /**
   * Submits an application to each of the supplied forms, aggregates them under one submission object along with
   * information about the submitter, automatically submits applications (to forms that are marked as such), retrieves
   * forms to which to redirect, and displays result messages.
   * @param sess
   * @param submissionData
   * @return
   */
  public SubmissionResult applyForMemberships(CurrentUser sess, PrefilledSubmissionData submissionData) {
    List<Application> applications = submissionData.getPrefilledData().stream()
                                         .map((data) -> applyForMembership(sess, data))
                                         .peek((app) -> app.setRedirectUrl(submissionData.getRedirectUrl()))
                                         .toList();

    Submission submission = new Submission();
    submission.setApplications(applications);

    setSubmissionMetadata(sess, submission);
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
   * @param sess
   * @param submission
   */
  private void setSubmissionMetadata(CurrentUser sess, Submission submission) {
    submission.setTimestamp(LocalDateTime.now());

    if (!sess.isAuthenticated()) {
      return;
    }

    if (idmService.getUserByIdentifier(sess.id()) != null) {
      submission.setSubmitterId(sess.id());
    }

    submission.setSubmitterName(sess.id());
    // TODO inquire which claims/attributes to store

    submission.setIdentityAttributes(sess.getAttributes().entrySet().stream()
                                         .filter(e -> e.getValue() != null)
                                         .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));

  }

  /**
   * Validates the input data for the form, marks assured prefilled items and submits an application.
   * @param sess
   * @param prefilledFormData
   * @return
   */
  public Application applyForMembership(CurrentUser sess, PrefilledFormData prefilledFormData) {
    Form form = formRepository.findByGroupId(prefilledFormData.getGroupId()).orElseThrow(() -> new IllegalArgumentException("Form not found"));

    if (prefilledFormData.getType() == Form.FormType.EXTENSION && !canExtendMembership(sess, prefilledFormData.getGroupId())) {
      throw new IllegalArgumentException("User cannot extend membership in group: " + prefilledFormData.getGroupId());
    }

    if (checkOpenApplications(sess, form) != null && prefilledFormData.getType() != Form.FormType.UPDATE) {
      // user has applied for membership since loading the form
      throw new IllegalArgumentException("User already has an open application in group: " + prefilledFormData.getGroupId());
    }

    Map<String, String> reservedPrincipalLogins = getReservedLoginsForPrincipal(sess); // call here to avoid unnecessary idm calls

    validateFilledFormData(sess, prefilledFormData);
    prefilledFormData.getPrefilledItems().forEach(item -> checkPrefilledValueConsistency(sess, item, reservedPrincipalLogins));

    Application app = new Application(0, Integer.parseInt(sess.id()), form.getId(),
        prefilledFormData.getPrefilledItems(), null, prefilledFormData.getType());
    try {
      app.submit(form);
    } catch (InvalidApplicationStateTransitionException e) {
      // should not happen, we checked the items beforehand
      throw new DataInconsistencyException(e.getMessage());
    }

    app = applicationRepository.save(app);

    reserveLogins(sess, prefilledFormData.getPrefilledItems());

    // TODO if we emit events asynchronously this might be problematic (not sure rollback would work here)
    eventService.emitEvent(new ApplicationSubmittedEvent(app.getId(), Integer.parseInt(sess.id()), prefilledFormData.getGroupId()));

    return app;
  }

  /**
   * Validates that the submitted form data is correctly filled in (e.g. required items not empty, values match
   * constraints, etc.)
   * @param sess
   * @param data
   */
  private void validateFilledFormData(CurrentUser sess, PrefilledFormData data) {
    List<FormItem> items = formService.getFormItems(sess, data.getForm(), data.getType());

    Set<Integer> itemIds = items.stream().map(FormItem::getId).collect(Collectors.toSet());
    List<FormItemData> foreignItems = data.getPrefilledItems().stream()
                                          .filter((itemData) -> itemIds.contains(itemData.getFormItem().getId()))
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
  private void checkPrefilledValueConsistency(CurrentUser sess, FormItemData itemData, Map<String, String> reservedLogins) {
    itemData.setValueAssured(false);
    if (StringUtils.isEmpty(itemData.getValue())) {
      return;
    }

    if (itemData.getFormItem().getType().equals(FormItem.Type.LOGIN)) {
      String loginValue = tryToFillLoginItem(sess, itemData.getFormItem(), reservedLogins);
      if (!StringUtils.isEmpty(loginValue) && loginValue.equals(itemData.getValue())) {
        itemData.setValueAssured(true);
        return;
      }
    }

    // TODO this is where we in the future want to handle attribute freshness/provenance logic

    String identityValue = getIdentityAttributeValue(sess, itemData.getFormItem());
    itemData.setIdentityAttributeValue(identityValue);
    String idmValue = getIdmAttributeValue(sess, itemData.getFormItem());
    itemData.setIdmAttributeValue(idmValue);

    if (Objects.equals(idmValue, itemData.getValue()) || Objects.equals(identityValue, itemData.getValue())) {
      itemData.setValueAssured(true);
    }
  }

  /**
   * Perform auto-submission of a form marked for auto submit. Use data from principal and the previous submission to
   * fill out form item data.
   * TODO run this asynchronously?
   * @param sess
   * @param form
   * @param submissionData
   */
  public void autoSubmitForm(CurrentUser sess, Form form, PrefilledSubmissionData submissionData) {
    PrefilledSubmissionData autoSubmitData;
    try {
      autoSubmitData = assemblePrefilledForms(sess, List.of(form), null);
      applyForMemberships(sess, autoSubmitData);
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
  private PrefilledFormData prefillAutosubmitFormItems(CurrentUser sess, Form form, List<FormItemData> prefilledItems) {
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
        submissionResult.setRedirectForms(loadForms(sess, groupIds, submissionResult.getRedirectUrl()));
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
   * Entry from GUI -> for a given group, determine which forms to display to the user, prefill form items and return to GUI
   *
   * @param sess
   * @param groupIds set of groups the user wants to apply for membership in
   * @return prefilled submission object, with the redirect URL and individual prefilled form data (prefilled items, form type) TODO should be enough to build the whole form page? or do we need more info?
   */
  public PrefilledSubmissionData loadForms(CurrentUser sess, List<Integer> groupIds, String redirectUrl) {
    List<Form> requiredForms = determineGroupSet(sess, groupIds);
    return assemblePrefilledForms(sess, requiredForms, redirectUrl);
  }

  /**
   * Determines the application types for each form, retrieves and prefills items for that form type and returns this
   * aggregated data.
   * @param sess
   * @param requiredForms
   * @param redirectUrl
   * @return
   */
  private PrefilledSubmissionData assemblePrefilledForms(CurrentUser sess, List<Form> requiredForms, String redirectUrl) {
    List<PrefilledFormData> prefilledFormData = new ArrayList<>();
    for (Form form : requiredForms) {
      if (checkOpenApplications(sess, form) != null) {
        // TODO open existing
        continue;
      }

      Form.FormType type = selectFormType(sess, form);
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

      form.setItems(formService.getFormItems(sess, form, type));

      List<FormItemData> prefilledFormItemData = loadForm(sess, form, type);

      prefilledFormData.add(new PrefilledFormData(form, form.getGroupId(), prefilledFormItemData,
          type));
    }

    // TODO probably some logic to order the individual forms? (e.g. prerequisites before their source form - or do we want
    //  to display prerequisites independently and have them redirect to the original forms?
    return new PrefilledSubmissionData(redirectUrl, prefilledFormData);
  }

  /**
   * For the group associated with the given form, determine the FormType (e.g. INITIAL, EXTENSION)
   * @param sess
   * @param form
   * @return
   */
  private Form.FormType selectFormType(CurrentUser sess, Form form) {

    if (checkUserMembership(sess, form.getGroupId())) {
      if (canExtendMembership(sess, form.getGroupId())) {
        return Form.FormType.EXTENSION;
      }
      // todo display error message if no forms returned?
      return null;
    }

    return Form.FormType.INITIAL;
  }

  /**
   * Generates prefilled form item data for the form and its type, calls module hooks and ensures the validity of form
   * item visibility.
   * @param sess
   * @param form
   * @param type
   * @return
   */
  public List<FormItemData> loadForm(CurrentUser sess, Form form, Form.FormType type) {
//    if (type.equals(Form.FormType.UPDATE)) {
//      // TODO prefill form with data from already submitted app if loading modifying/update form type
//    }

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    modules.forEach(module -> module.getFormModule().canBeSubmitted(sess, type, module.getOptions()));

    List<FormItemData> prefilledFormItemData = prefillForm(sess, form);

    modules.forEach(module -> module.getFormModule().afterFormItemsPrefilled(sess, type, prefilledFormItemData));

    checkMissingPrefilledItems(sess, prefilledFormItemData);

    return prefilledFormItemData;
  }

  /**
   * Returns prefilled form item data for supplied form's items.
   * Allow admin to define where to fill item from (e.g. federation, IdM, more complex solutions/external sources -> from submitted prerequisites)]
   * TODO we can also hide prerequisite submitted apps behind external sources (same with redirect forms)
   * @param sess
   * @param form Form object with set form items array
   * @return
   */
  private List<FormItemData> prefillForm(CurrentUser sess, Form form) {
    Map<String, String> reservedPrincipalLogins = getReservedLoginsForPrincipal(sess); // call here to avoid unnecessary idm calls

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
  private void checkMissingPrefilledItems(CurrentUser sess, List<FormItemData> prefilledItems) {
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
  private String calculatePrefilledValue(CurrentUser sess, FormItem item, Map<String, String> reservedLogins) { // again decide whether to pass principal as argument or retrieve it from the current session
   if (item.getType().equals(FormItem.Type.LOGIN)) {
     String login = tryToFillLoginItem(sess, item, reservedLogins);
     if (!StringUtils.isEmpty(login)) {
       return login;
     }
   }

    String identityValue = getIdentityAttributeValue(sess, item);
    if (item.isPreferIdentityAttribute() && identityValue != null) {
      return identityValue;
    }
    String idmValue = getIdmAttributeValue(sess, item);
    if (idmValue != null) {
      return idmValue;
    } else if (identityValue != null) {
      return identityValue;
    }
    return item.getDefaultValue();
  }

  /**
   * Retrieves value of item's source identity attribute
   * @param sess
   * @param item
   * @return
   */
  private String getIdentityAttributeValue(CurrentUser sess, FormItem item) {
    String sourceAttr = item.getSourceIdentityAttribute();
    return sourceAttr == null ? null : sess.attribute(sourceAttr);
  }

  /**
   * Retrieves value of item's source IdM attribute.
   * @param sess
   * @param item
   * @return
   */
  private String getIdmAttributeValue(CurrentUser sess, FormItem item) {
    Form form = formRepository.findById(item.getFormId()).orElseThrow(() -> new DataInconsistencyException("Form with ID " + item.getFormId() + " not found for form item " + item.getId()));

    String sourceAttr = item.getSourceIdmAttribute();
    if (sourceAttr == null) {
      return null;
    }
    if (sourceAttr.startsWith("urn:perun:user")) {
      return idmService.getUserAttribute(String.valueOf(sess.id()), sourceAttr);
    } else if (sourceAttr.startsWith("urn:perun:vo")) {
      return idmService.getVoAttribute(sourceAttr, form.getVoId());
    } else if (sourceAttr.startsWith("urn:perun:member") && form.getGroupId() > 0) { // TODO better check if group is present
      return idmService.getMemberAttribute(String.valueOf(sess.id()), sourceAttr, form.getGroupId());
    } else if (sourceAttr.startsWith("urn:perun:group") && form.getGroupId() > 0) {
      return idmService.getGroupAttribute(sourceAttr, form.getGroupId());
    } else {
      throw new IllegalArgumentException("Unsupported attribute source: " + sourceAttr);
    }
  }

  /**
   * Tries to fill items with the destination attribute matching the LOGIN urn
   * @param sess
   * @param item
   * @param reservedLogins
   * @return
   */
  private String tryToFillLoginItem(CurrentUser sess, FormItem item, Map<String, String> reservedLogins) {
    for (String namespace : reservedLogins.keySet()) {
      String loginAttributeDefinition = "urn:perun:user:attribute-def:def:login-namespace:" + namespace;  // TODO add config property or constant
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
  private void reserveLogins(CurrentUser sess, List<FormItemData> itemData) {

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
   * @param sess
   * @return a map of reserved logins, the keys being namespaces of the logins
   */
  private Map<String, String> getReservedLoginsForPrincipal(CurrentUser sess) {
    return new HashMap<>();
  }


  /**
   * Check whether user already has open application in the given group/VO (for the given form). If so, use this information
   * to determine FormType (e.g., if user has open application, update this application -  TODO do we use separate application type or just update the data?)
   * TODO also check by data stored in the Submission object
   * @param form
   * @param sess
   */
  private Application checkOpenApplications(CurrentUser sess, Form form) {
    if (sess.isAuthenticated()) {
      Optional<Application> foundApp = applicationRepository.findByFormId(form.getId()).stream()
          .filter(app -> app.getState().isOpenState())
          .filter(app -> app.getUserId() == Integer.parseInt(sess.id()))
                                .findFirst(); // TODO utilize similarUsers as well?
      return foundApp.orElse(null);
    }
    return null;
  }

  /**
   * Check whether user is a member of the group/VO in the underlying IdM. If so, use this information to determine
   * FormType (e.g if user is member and is can extend, use EXTENSION form type) TODO UPDATE and CANCELLATION form types via different endpoint?
   * @param sess
   * @param groupId
   */
  private boolean checkUserMembership(CurrentUser sess, int groupId) {
    if (sess.isAuthenticated()) {
      return idmService.getGroupIdsWhereUserIsMember(sess.id()).contains(groupId);
    }
    return false;
  }

  /**
   * Check whether user can extend membership in the group/VO in the underlying IdM.
   * TODO Either use a direct IdM call or operate using membershipExpiration attribute? Prolly direct call, more logic involved (rules, etc.)
   * operate using
   * @param sess
   * @param groupId
   * @return
   */
  private boolean canExtendMembership(CurrentUser sess, int groupId) {
    if (sess.isAuthenticated() && checkUserMembership(sess, groupId)) {
      return idmService.canExtendMembership(sess.id(), groupId);
    }
    return false;
  }

  /**
   * Determine which groups to submit applications to based on the input parameters.
   * Meaning determine prerequisites for group based on form relationships, based on the input parameters (can be a list of ids/names)
   * @param sess
   * @param groupIds TODO not sure what the input parameters will be
   * @return list of group ids to submit applications to
   */
  private List<Form> determineGroupSet(CurrentUser sess, List<Integer> groupIds) {
    List<Form> requiredForms = new ArrayList<>();
    for (Integer groupId : groupIds) {
      Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form for group " + groupId + " not found"));
      // TODO do we always display EXTENSION forms if user is already member of some groups?

      requiredForms.add(form);
    }
    return requiredForms;
  }
}
