package org.perun.registrarprototype.services;

import java.util.List;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.ApplicationRepositoryDummy;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormRepositoryDummy;

public class ApplicationService {
  private final ApplicationRepository applicationRepository = new ApplicationRepositoryDummy();
  private final FormRepository formRepository = new FormRepositoryDummy();
  private final NotificationService notificationService = new NotificationServiceDummy();
  private final PerunIntegrationService perunIntegrationService = new PerunIntegrationDummy();

  public ApplicationService() {}


  public Application registerUserToGroup(int userId, int groupId, int formId, List<FormItemData> itemData) {
    Form form = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form not found"));

    Application app = new Application(applicationRepository.getNextId(), userId, groupId, formId, itemData);
    try {
      app.submit(form);
    } catch (InvalidApplicationDataException e) {
      // handle logs, feedback to GUI
      throw new RuntimeException(e);
    }
    app.approve();
    applicationRepository.save(app);
    notificationService.notifyApplicationApproved();

    perunIntegrationService.registerUserToGroup(userId, groupId);

    return app;
  }
}
