package org.perun.registrarprototype.services;

import java.util.List;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.ApplicationRepositoryDummy;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormRepositoryDummy;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
  private final ApplicationRepository applicationRepository = new ApplicationRepositoryDummy();
  private final FormRepository formRepository = new FormRepositoryDummy();
  private final NotificationService notificationService = new NotificationServiceDummy();
  private final PerunIntegrationService perunIntegrationService = new PerunIntegrationDummy();
  private final AuthorizationService authorizationService = new AuthorizationServiceDummy();

  public ApplicationService() {}


  public Application registerUserToGroup(int userId, int groupId, List<FormItemData> itemData) {
    Application app = this.applyForMembership(userId, groupId, itemData);

    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }
    applicationRepository.save(app);
    notificationService.notifyApplicationApproved();

    perunIntegrationService.registerUserToGroup(userId, groupId);

    return app;
  }

  public Application applyForMembership(int userId, int groupId, List<FormItemData> itemData) {
    System.out.println(itemData);
    Form form = formRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Form not found"));

    Application app = new Application(applicationRepository.getNextId(), userId, groupId, form.getId(), itemData);
    try {
      app.submit(form);
    } catch (InvalidApplicationDataException e) {
      // handle logs, feedback to GUI
      throw new RuntimeException(e);
    }
    applicationRepository.save(app);
    notificationService.notifyApplicationSubmitted();

    return app;
  }

  public void approveApplication(int applicationId) {
    Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Application not found"));

    if (!authorizationService.canApprove(app.getId())) {
      // return 403
      throw new RuntimeException();
    }
    try {
      app.approve();
    } catch (InvalidApplicationStateTransitionException e) {
      throw new RuntimeException(e);
    }
    applicationRepository.save(app);
    notificationService.notifyApplicationApproved();

    perunIntegrationService.registerUserToGroup(app.getUserId(), app.getGroupId());
  }
}
