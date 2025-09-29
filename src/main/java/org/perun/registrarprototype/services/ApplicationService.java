package org.perun.registrarprototype.services;

import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.PrefilledFormData;
import org.perun.registrarprototype.models.PrefilledSubmissionData;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.models.ValidationError;
import org.perun.registrarprototype.repositories.ApplicationRepository;
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
  private final EventService eventService;
  private final PerunIntegrationService perunIntegrationService;
  private final AuthorizationService authorizationService;
  private final FormService formService;
  private final IdMService idmService;

  public ApplicationService(ApplicationRepository applicationRepository, FormRepository formRepository,
                            SubmissionRepository submissionRepository,
                            EventService eventService, PerunIntegrationService perunIntegrationService,
                            AuthorizationService authorizationService, FormService formService,
                            IdMService idmService) {
    this.applicationRepository = applicationRepository;
    this.formRepository = formRepository;
    this.submissionRepository = submissionRepository;
    this.eventService = eventService;
    this.perunIntegrationService = perunIntegrationService;
    this.authorizationService = authorizationService;
    this.formService = formService;
    this.idmService = idmService;
  }

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

  public Application approveApplication(CurrentUser sess, int applicationId) throws InsufficientRightsException {
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
    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }


    app = applicationRepository.save(app);

    for (AssignedFormModule module : modules) {
      module.getFormModule().onApproval(app);
    }
    // TODO directly call IdM (this could be done via module) / call adapter (again this could be done via modules) / or possibly emit event with whole app object + submission data
    perunIntegrationService.registerUserToGroup(app.getUserId(), form.getGroupId());

    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), app.getUserId(), form.getGroupId()));
    return app;
  }

  public Application rejectApplication(CurrentUser sess, int applicationId) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(sess, app.getFormId());

    if (!authorizationService.isAuthorized(sess, form.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to reject this application");
    }
    try {
      app.reject("Manually rejected by manager");
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    for (AssignedFormModule module : modules) {
      module.getFormModule().onRejection(app);
    }

    app = applicationRepository.save(app);
    eventService.emitEvent(new ApplicationRejectedEvent(app.getId(), app.getUserId(), form.getGroupId()));

    return app;
  }

  public Application requestChanges(CurrentUser sess, int applicationId) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formService.getFormById(sess, app.getFormId());

    if (!authorizationService.isAuthorized(sess, form.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to request changes to this application");
    }

    try {
      app.requestChanges();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    app = applicationRepository.save(app);
    eventService.emitEvent(new ChangesRequestedToApplicationEvent(app.getId(), app.getUserId(), form.getGroupId()));

    return app;
  }

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
    List<Form> autoSubmitForms = submissionData.getPrefilledData().stream()
                                    .flatMap(formData -> formService.getAutosubmitForms(formData.getForm()).stream())
                                     .toList();
    autoSubmitForms.forEach(form -> {
      autoSubmitForm(sess, form, submissionData);
      result.addMessage("Form into group " + form.getGroupId() + " was submitted automatically.");
    });

    List<Form> redirectForms = submissionData.getPrefilledData().stream()
                                   .flatMap(formData -> formService.getRedirectForms(formData.getForm()).stream())
                                    .toList();
    result.setRedirectUrl(submissionData.getRedirectUrl());
    setRedirectForms(sess, redirectForms, result);

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

  public Application applyForMembership(CurrentUser sess, PrefilledFormData prefilledFormData) {
    Form form = formRepository.findByGroupId(prefilledFormData.getGroupId()).orElseThrow(() -> new IllegalArgumentException("Form not found"));

    if (prefilledFormData.getType() == Form.FormType.EXTENSION && !canExtendMembership(sess, prefilledFormData.getGroupId())) {
      throw new IllegalArgumentException("User cannot extend membership in group: " + prefilledFormData.getGroupId());
    }

    if (checkOpenApplications(sess, form) != null && prefilledFormData.getType() != Form.FormType.UPDATE) {
      // user has applied for membership since loading the form
      throw new IllegalArgumentException("User already has an open application in group: " + prefilledFormData.getGroupId());
    }

    prefilledFormData.getPrefilledItems().forEach(item -> {
      validateFilledFormItemData(sess, form, item);
      checkPrefilledValueConsistency(sess, item);
    });

    Application app = new Application(0, Integer.parseInt(sess.id()), form.getId(),
        prefilledFormData.getPrefilledItems(), null, prefilledFormData.getType());
    try {
      app.submit(form);
    } catch (InvalidApplicationDataException | InvalidApplicationStateTransitionException e) {
      // should not happen, we checked the items beforehand
      throw new DataInconsistencyException(e.getMessage());
    }

    // TODO if we emit events asynchronously this might be problematic (not sure rollback would work here)
    app = applicationRepository.save(app);
    eventService.emitEvent(new ApplicationSubmittedEvent(app.getId(), Integer.parseInt(sess.id()), prefilledFormData.getGroupId()));

    return app;
  }

  /**
   * Validate that the given form item data is valid (e.g. not empty, valid value, etc.)
   * @param sess
   * @param form
   * @param itemData
   */
  private void validateFilledFormItemData(CurrentUser sess, Form form, FormItemData itemData) {
    // retrieve form item again to check whether it hasn't changed since submission
    FormItem item = formService.getFormItemById(sess, itemData.getFormItem().getId());

    if (item.getFormId() != form.getId()) {
      throw new IllegalArgumentException("Form item " + item.getId() + " does not belong to form " + form.getId());
    }

    ValidationError validation = item.validate(itemData);

    if (validation != null) {
      throw new IllegalArgumentException("Form item " + item.getId() + " validation failed with error: " + validation.message());
    }

    itemData.setFormItem(item);
  }

  /**
   * Checks that form data is validated against form item constraints, checks that prefilled data is still valid, if so
   * set a flag to indicate that the form item data has been prefilled and validated (LOA or just a flag).
   * @param sess
   * @param itemData
   */
  private void checkPrefilledValueConsistency(CurrentUser sess, FormItemData itemData) {
    itemData.setValueAssured(false);
    if (StringUtils.isEmpty(itemData.getValue())) {
      return;
    }

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
   * @return map, keys being groupIds, the value a list of prefilled FormItems TODO should be enough to build the whole form page? or do we need more info?
   */
  public PrefilledSubmissionData loadForms(CurrentUser sess, List<Integer> groupIds, String redirectUrl) {
    List<Form> requiredForms = determineGroupSet(sess, groupIds);
    return assemblePrefilledForms(sess, requiredForms, redirectUrl);
  }

  private PrefilledSubmissionData assemblePrefilledForms(CurrentUser sess, List<Form> requiredForms, String redirectUrl) {
    List<PrefilledFormData> prefilledFormData = new ArrayList<>();
    for (Form form : requiredForms) {
      Form.FormType type = selectFormType(sess, form);

      List<FormItem> formItems = formService.getFormItems(sess, form, type);

      List<FormItemData> prefilledFormItemData = loadForm(sess, form, type, formItems);

      prefilledFormData.add(new PrefilledFormData(form, form.getGroupId(), prefilledFormItemData,
          type));
    }

    return new PrefilledSubmissionData(redirectUrl, prefilledFormData);
  }

  /**
   * For the group associated with the given form, determine the FormType (e.g. INITIAL, EXTENSION)
   * @param sess
   * @param form
   * @return
   */
  private Form.FormType selectFormType(CurrentUser sess, Form form) {

    Application existingApplication = checkOpenApplications(sess, form);
    if (existingApplication != null) {
      // TODO This is probably a bit problematic -> we probably want to know we're updating an INITIAL X EXTENSION app,
      //  also, do we prefill the prefilled data again if updating? Probably call a different method for updating
      return Form.FormType.UPDATE;
    }

    if (canExtendMembership(sess, form.getGroupId())) {
      return Form.FormType.EXTENSION;
    }


    return Form.FormType.INITIAL;
  }

  public List<FormItemData> loadForm(CurrentUser sess, Form form, Form.FormType type, List<FormItem> formItems) {

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    modules.forEach(module -> module.getFormModule().canBeSubmitted(sess, type, module.getOptions()));

    List<FormItemData> prefilledFormItemData = formItems.stream()
        .map((item) -> this.prefillFormItemValue(sess, item))
        .toList();

    // TODO prefill form with data from already submitted app if loading modifying/update form type (e.g pass form type as arg)

    modules.forEach(module -> module.getFormModule().afterFormItemsPrefilled(sess, type, prefilledFormItemData));

    return prefilledFormItemData;
  }

  /**
   * Prefill form item value based on principal data (to be returned to GUI).
   * @param item
   * @return
   */
  private FormItemData prefillFormItemValue(CurrentUser sess, FormItem item) { // again decide whether to pass principal as argument or retrieve it from the current session
    String identityValue = getIdentityAttributeValue(sess, item);
    if (item.isPreferIdentityAttribute() && identityValue != null) {
      return new FormItemData(item, null, identityValue);
    }
    String idmValue = getIdmAttributeValue(sess, item);
    if (idmValue != null) {
      return new FormItemData(item, null, idmValue);
    } else if (identityValue != null) {
      return new FormItemData(item, null, identityValue);
    }
    return new FormItemData(item, null, item.getDefaultValue());
  }

  private String getIdentityAttributeValue(CurrentUser sess, FormItem item) {
    String sourceAttr = item.getSourceIdentityAttribute();
    return sourceAttr == null ? null : sess.attribute(sourceAttr);
  }

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

      requiredForms.addAll(formService.getPrerequisiteForms(form));
      requiredForms.add(form);
    }
    return requiredForms;
  }


}
