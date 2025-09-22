package org.perun.registrarprototype.services;

import java.util.List;
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
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
  private final ApplicationRepository applicationRepository;
  private final FormRepository formRepository;
  private final EventService eventService;
  private final PerunIntegrationService perunIntegrationService;
  private final AuthorizationService authorizationService;
  private final FormService formService;

  public ApplicationService(ApplicationRepository applicationRepository, FormRepository formRepository, EventService eventService, PerunIntegrationService perunIntegrationService, AuthorizationService authorizationService, FormService formService) {
    this.applicationRepository = applicationRepository;
    this.formRepository = formRepository;
    this.eventService = eventService;
    this.perunIntegrationService = perunIntegrationService;
    this.authorizationService = authorizationService;
    this.formService = formService;
  }

  public Application registerUserToGroup(int userId, int groupId, List<FormItemData> itemData)
      throws InvalidApplicationDataException {
    Application app = this.applyForMembership(userId, groupId, itemData);

    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }
    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), userId, groupId));

    perunIntegrationService.registerUserToGroup(userId, groupId);

    return app;
  }

  public Application applyForMembership(int userId, int groupId, List<FormItemData> itemData)
      throws InvalidApplicationDataException {
    // probably better to replace userId with the CurrentUser implementation
    Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form not found"));

    // TODO prefill form with data from principal (and potentially IdM attributes?) -> this will likely need to be done when initially retrieving the form for GUI,
    //  a check that FormItems flagged as prefilled have not been modified / are ignored here

    Application app = new Application(applicationRepository.getNextId(), userId, form.getId(), itemData);
    try {
      // TODO how to implement `canBeSubmitted` module hook currently sync in Perun? Same with `canBeApproved`, `beforeApprove`, etc.
      app.submit(form);
    } catch (InvalidApplicationDataException e) {
      // handle logs, feedback to GUI
      throw e;
    }
    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationSubmittedEvent(app.getId(), userId, groupId));

    return app;
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

  public Application loadForm(CurrentUser sess, int formId, String redirectUrl) {
    Form form = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    checkOpenApplications(sess, form);

    // load modules before prefilling to avoid potential pointless computation
    List<AssignedFormModule> modules = formService.getAssignedFormModules(form);

    List<FormItemData> prefilledFormItemData = form.getItems().stream()
        .map(this::prefillFormItemValue)
        .toList();

    // Create application object to hold the prefilled data, id and user id is not needed for now
    Application app = new Application(-1, -1, formId, prefilledFormItemData);


    // TODO this will likely need a more complex solution to handle chaining/combined forms
    app.setRedirectUrl(redirectUrl);

    modules.forEach(module -> module.getFormModule().beforeSubmission(app, module.getOptions()));

    return app;
  }

  /**
   * Prefill form item value based on principal data.
   * @param item
   * @return
   */
  private FormItemData prefillFormItemValue(FormItem item) { // again decide whether to pass principal as argument or retrieve it from the current session
    return null;
  }


  /**
   * Check whether user already has open application in the given group/VO (for the given form).
   * @param form
   * @param sess
   */
  private void checkOpenApplications(CurrentUser sess, Form form) {
    // TODO check for open applications if user already exists - define how to check whether user does exist in th system
    if (sess.id() > 0) {
      boolean hasOpenApps = applicationRepository.findByFormId(form.getId()).stream()
          .filter(app -> app.getState().isOpenState())
          .anyMatch(app -> app.getUserId() == sess.id()); // TODO utilize similarUsers as well?
      if (hasOpenApps) {
        throw new IllegalArgumentException("User already has open application for form " + form.getId());
      }
    }
  }


}
