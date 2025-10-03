package org.perun.registrarprototype.core.services;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.core.models.Application;
import org.perun.registrarprototype.extension.dto.ApplicationState;
import org.perun.registrarprototype.core.models.AssignedFormModule;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.core.models.FormItem;
import org.perun.registrarprototype.core.models.FormItemData;
import org.perun.registrarprototype.core.models.PrefilledFormData;
import org.perun.registrarprototype.core.security.CurrentUser;
import org.perun.registrarprototype.extension.dto.FormType;
import org.perun.registrarprototype.extension.dto.ItemType;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationServiceTests extends GenericRegistrarServiceTests {

//  @Test
//  void registerUserIntoGroup() throws Exception {
//
//    int userId = 1;
//    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
//    item1 = formService.createFormItem(item1);
//    FormItem item2 = new FormItem(2, FormItem.Type.TEXTFIELD);
//    item2 = formService.createFormItem(item2);
//    int groupId = 1;
//    perunIntegrationService.createGroup(groupId);
//    Form form = new Form(formRepository.getNextId(), groupId, List.of(item1, item2));
//    formRepository.save(form);
//    FormItemData formItemData1 = new FormItemData(item1, "test1");
//    FormItemData formItemData2 = new FormItemData(item2, "test2");
//
//    Application application = applicationService.registerUserToGroup(new CurrentUser(userId, null), groupId,
//        List.of(formItemData1, formItemData2));
//
//    assert application.getState() == ApplicationState.APPROVED;
//    assert perunIntegrationService.isUserMemberOfGroup(groupId, userId);
//  }

  @Test
  void applyWithCorrectItemConstraints() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);
    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(form.getId(), item1);


    FormItemData formItemData1 = new FormItemData(item1, "test@gmail.com");

    Application app = applicationService.applyForMembership(new CurrentUser(-1, null), new PrefilledFormData(form, groupId, List.of(formItemData1), FormType.INITIAL));

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.SUBMITTED;
  }

  @Test
  void approveApplication() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(form.getId(), item1);

    FormItemData formItemData1 = new FormItemData(item1, "test@gmail.com");

    Application app = applicationService.applyForMembership(new CurrentUser(-1, null), new PrefilledFormData(form, groupId, List.of(formItemData1), FormType.INITIAL));

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.SUBMITTED;

    applicationService.approveApplication(new CurrentUser(-1, null), createdApp.getId(), "");

    createdApp = applicationRepository.findById(app.getId()).orElse(null);
    assert createdApp != null;
    assert createdApp.getState().equals(ApplicationState.APPROVED);
  }

  @Test
  void loadForm() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(form.getId(), item1);

    List<FormItemData> data = applicationService.loadForm(new CurrentUser(-1, null), form, FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == item1.getId();
  }

  @Test
  void loadFormCallsAfterFormItemsPrefilledHook() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);

    AssignedFormModule module = new AssignedFormModule("testModuleBeforeSubmission", new HashMap<>());

    formModuleService.setModules(null, form, List.of(module));

    List<FormItemData> data = applicationService.loadForm(new CurrentUser(-1, null), form, FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == item1.getId();
    assert data.getFirst().getPrefilledValue().equals("testModuleValue");
  }

  @Test
  void loadFormPrefillsValues() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);
    Form form = formService.createForm(null, groupId);


    FormItem item1 = new FormItem(1, ItemType.TEXTFIELD);
    item1.setSourceIdentityAttribute("testAttribute");
    item1 = formService.setFormItem(form.getId(), item1);

    List<FormItemData> data = applicationService.loadForm(currentUserProvider.getCurrentUser(""), form, FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == item1.getId();
    assert data.getFirst().getPrefilledValue().equals("testValue");
  }

}
