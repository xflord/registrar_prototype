package org.perun.registrarprototype.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.ApplicationForm;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
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
    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(formSpecification.getId(), item1);


    FormItemData formItemData1 = new FormItemData(item1, "test@gmail.com");

    Application app = applicationService.applyForMembership(new ApplicationForm(formSpecification, List.of(formItemData1), FormSpecification.FormType.INITIAL), submission, "");

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.SUBMITTED;
  }

  @Test
  void approveApplication() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    FormItemData formItemData1 = new FormItemData(item1, "test@gmail.com");

    Application app = applicationService.applyForMembership(new ApplicationForm(formSpecification, new ArrayList<>(List.of(formItemData1)), FormSpecification.FormType.INITIAL), submission, "");

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.SUBMITTED;

    applicationService.approveApplication(sessionProvider.getCurrentSession(), createdApp.getId(), "");

    createdApp = applicationRepository.findById(app.getId()).orElse(null);
    assert createdApp != null;
    assert createdApp.getState().equals(ApplicationState.APPROVED);
  }

  @Test
  void loadForm() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    List<FormItemData> data = applicationService.loadForm(sessionProvider.getCurrentSession(), formSpecification, FormSpecification.FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == item1.getId();
  }

  @Test
  void loadFormCallsAfterFormItemsPrefilledHook() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule module = new AssignedFormModule("testModuleBeforeSubmission", new HashMap<>());

    formService.setModules(null, formSpecification.getId(), List.of(module));

    List<FormItemData> data = applicationService.loadForm(sessionProvider.getCurrentSession(), formSpecification, FormSpecification.FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == item1.getId();
    assert data.getFirst().getPrefilledValue().equals("testModuleValue");
  }

  @Test
  void loadFormPrefillsValues() throws Exception {
    int groupId = getGroupId();
    FormSpecification formSpecification = formService.createForm(groupId);


    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    Map<String, String> prefillStrategyOptions = new HashMap<>();
    item1.addPrefillStrategyEntry(new PrefillStrategyEntry(FormItem.PrefillStrategyType.IDENTITY_ATTRIBUTE, prefillStrategyOptions, "testAttribute"));
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    List<FormItemData> data = applicationService.loadForm(sessionProvider.getCurrentSession(), formSpecification, FormSpecification.FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == item1.getId();
    assert data.getFirst().getPrefilledValue().equals("testValue");
  }
}
