package org.perun.registrarprototype.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.perun.registrarprototype.events.ApplicationApprovedEvent;
import org.perun.registrarprototype.events.ApplicationRejectedEvent;
import org.perun.registrarprototype.events.ApplicationSubmittedEvent;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
  private final ApplicationRepository applicationRepository;
  private final FormRepository formRepository;
  private final EventService eventService;
  private final PerunIntegrationService perunIntegrationService;
  private final AuthorizationService authorizationService;
  private final FormService formService;
  private final IdMService idmService;

  public ApplicationService(ApplicationRepository applicationRepository, FormRepository formRepository,
                            EventService eventService, PerunIntegrationService perunIntegrationService,
                            AuthorizationService authorizationService, FormService formService,
                            IdMService idmService) {
    this.applicationRepository = applicationRepository;
    this.formRepository = formRepository;
    this.eventService = eventService;
    this.perunIntegrationService = perunIntegrationService;
    this.authorizationService = authorizationService;
    this.formService = formService;
    this.idmService = idmService;
  }

  public Application registerUserToGroup(CurrentUser sess, int groupId, List<FormItemData> itemData)
      throws InvalidApplicationDataException {
    Application app = this.applyForMembership(sess, groupId, Form.FormType.INITIAL, itemData);

    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }
    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), Integer.parseInt(sess.id()), groupId));

    perunIntegrationService.registerUserToGroup(Integer.parseInt(sess.id()), groupId);

    return app;
  }

  public Application applyForMembership(CurrentUser sess, int groupId, Form.FormType type, List<FormItemData> itemData)
      throws InvalidApplicationDataException {
    Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form not found"));

    if (type == Form.FormType.EXTENSION && !canExtendMembership(sess, groupId)) {
      throw new IllegalArgumentException("User cannot extend membership in this group");
    }

    if (checkOpenApplications(sess, form) != null && type != Form.FormType.UPDATE) {
      // user has applied for membership since loading the form
      throw new IllegalArgumentException("User already has an open application in this group");
    }

    itemData.forEach(item -> validateFilledFormItemData(sess, form, item));

    Application app = new Application(applicationRepository.getNextId(), Integer.parseInt(sess.id()), form.getId(), itemData, null, type);
    try {
      app.submit(form);
    } catch (InvalidApplicationDataException e) {
      // handle logs, feedback to GUI
      throw e;
    }
    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationSubmittedEvent(app.getId(), Integer.parseInt(sess.id()), groupId));

    return app;
  }

  /**
   * Checks that form data is validated against form item constraints, checks that prefilled data is still valid, if so
   * set a flag to indicate that the form item data has been prefilled and validated (LOA or just a flag).
   * @param sess
   * @param form
   * @param itemData
   */
  private void validateFilledFormItemData(CurrentUser sess, Form form, FormItemData itemData) {
    // will GUI set the prefilled value into `value` or keep it only in the prefilled field? minor detail ig
    if (itemData.getValue() != null &&
            Objects.equals(itemData.getValue(), prefillFormItemValue(sess, itemData.getFormItem()).getValue())) {
      itemData.setPrefilledValue(itemData.getValue());
      itemData.setValueAssured(true);
    }
  }

  public void approveApplication(CurrentUser sess, int applicationId) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formRepository.findById(app.getFormId()).orElseThrow(() -> new DataInconsistencyException("Form with ID " + app.getFormId() + " not found for application " + app.getId()));

    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);
    modules.forEach(module -> module.getFormModule().beforeApproval(app));

    if (!authorizationService.isAuthorized(sess, form.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to approve this application");
    }
    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }

    modules.forEach(module -> module.getFormModule().onApproval(app));

    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), app.getUserId(), form.getGroupId()));

    perunIntegrationService.registerUserToGroup(app.getUserId(), form.getGroupId());
  }

  public void rejectApplication(CurrentUser sess, int applicationId) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    Form form = formRepository.findById(app.getFormId()).orElseThrow(() -> new DataInconsistencyException("Form with ID " + app.getFormId() + " not found for application " + app.getId()));

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
    modules.forEach(module -> module.getFormModule().onRejection(app));

    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationRejectedEvent(app.getId(), app.getUserId(), form.getGroupId()));
  }

  /**
   * Entry from GUI -> for a given group, determine which forms to display to the user.
   *
   * @param sess
   * @param groupIds set of groups the user wants to apply for membership in
   * @return map, keys being groupIds, the value a list of prefilled FormItems TODO should be enough to build the whole form page? or do we need more info?
   */
  private Map<Integer, List<FormItemData>> selectForms(CurrentUser sess, List<Integer> groupIds) {
    List<Form> requiredForms = new ArrayList<>();
    for (Integer groupId : groupIds) {
      Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form for group " + groupId + " not found"));

      requiredForms.addAll(formService.getPrerequisiteForms(form));
      requiredForms.add(form);
    }

    Map<Integer, List<FormItemData>> result = new HashMap<>();
    for (Form form : requiredForms) {
      Form.FormType type = selectFormType(sess, form);

      List<FormItem> formItems = formService.getFormItems(sess, form, type);

      List<FormItemData> prefilledFormItemData = loadForm(sess, form, type, formItems);

      result.put(form.getGroupId(), prefilledFormItemData);
    }

    return result;
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
   * @param form
   * @param sess
   */
  private Application checkOpenApplications(CurrentUser sess, Form form) {
    // TODO check for open applications if user already exists - define how to check whether user does exist in th system
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
   * @param formId TODO not sure what the input parameters will be
   * @return list of group ids to submit applications to
   */
  private List<Integer> determineGroupSet(CurrentUser sess, int formId) {
    return new ArrayList<>();
  }


}
