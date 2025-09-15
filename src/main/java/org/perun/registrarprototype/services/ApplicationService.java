package org.perun.registrarprototype.services;

import java.util.List;
import org.perun.registrarprototype.events.ApplicationApprovedEvent;
import org.perun.registrarprototype.events.ApplicationRejectedEvent;
import org.perun.registrarprototype.events.ApplicationSubmittedEvent;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.ApplicationRepositoryDummy;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormRepositoryDummy;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.services.tempImpl.PerunIntegrationDummy;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
  private final ApplicationRepository applicationRepository = new ApplicationRepositoryDummy();
  private final FormRepository formRepository = new FormRepositoryDummy();
  private final EventService eventService = new EventServiceImpl();
  private final PerunIntegrationService perunIntegrationService = new PerunIntegrationDummy();
  private AuthorizationService authorizationService = new AuthorizationServiceImpl();

  public ApplicationService() {}

  public ApplicationService(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
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
    System.out.println(itemData);
    Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form not found"));

    // TODO prefill form with data from principal (and potentially IdM attributes?)

    Application app = new Application(applicationRepository.getNextId(), groupId, userId, form.getId(), itemData);
    try {
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

    if (!authorizationService.isAuthorized(sess, app.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to approve this application");
    }
    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }
    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationApprovedEvent(app.getId(), app.getUserId(), app.getGroupId()));

    perunIntegrationService.registerUserToGroup(app.getUserId(), app.getGroupId());
  }

  public void rejectApplication(CurrentUser sess, int applicationId) throws InsufficientRightsException {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));

    if (!authorizationService.isAuthorized(sess, app.getGroupId())) {
      // return 403
      throw new InsufficientRightsException("You are not authorized to approve this application");
    }
    try {
      app.reject("Manually rejected by manager");
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }
    applicationRepository.save(app);
    eventService.emitEvent(new ApplicationRejectedEvent(app.getId(), app.getUserId(), app.getGroupId()));
  }
}
