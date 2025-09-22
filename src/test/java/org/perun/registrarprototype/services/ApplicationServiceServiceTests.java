package org.perun.registrarprototype.services;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationServiceServiceTests extends GenericRegistrarServiceTests {

  @Test
  void registerUserIntoGroup() throws Exception {

    int userId = 1;
    FormItem item1 = new FormItem(1, "test");
    FormItem item2 = new FormItem(2, "test");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);
    Form form = new Form(formRepository.getNextId(), groupId, List.of(item1, item2));
    formRepository.save(form);
    FormItemData formItemData1 = new FormItemData(item1.getId(), "test1");
    FormItemData formItemData2 = new FormItemData(item2.getId(), "test2");

    Application application = applicationService.registerUserToGroup(userId, groupId,
        List.of(formItemData1, formItemData2));

    assert application.getState() == ApplicationState.APPROVED;
    assert perunIntegrationService.isUserMemberOfGroup(groupId, userId);
  }

  @Test
  void applyWithCorrectItemConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1));

    FormItemData formItemData1 = new FormItemData(item1.getId(), "test@gmail.com");

    Application app = applicationService.applyForMembership(-1, groupId, List.of(formItemData1));

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.PENDING;
  }

  @Test
  void approveApplication() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1));

    FormItemData formItemData1 = new FormItemData(item1.getId(), "test@gmail.com");

    Application app = applicationService.applyForMembership(-1, groupId, List.of(formItemData1));

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.PENDING;

    applicationService.approveApplication(null, createdApp.getId());

    createdApp = applicationRepository.findById(app.getId()).orElse(null);
    assert createdApp != null;
    assert createdApp.getState().equals(ApplicationState.APPROVED);
  }

}
