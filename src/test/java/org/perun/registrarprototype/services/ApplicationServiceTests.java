package org.perun.registrarprototype.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.ApplicationForm;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationServiceTests extends GenericRegistrarServiceTests {

//  @Test
//  void registerUserIntoGroup() throws Exception {
//
//    int userId = 1;
//    ItemDefinition itemDef1 = createItemDefinition(ItemType.TEXTFIELD, "item1", false, null);
//    FormItem item1 = formService.createFormItem(createFormItem(0, itemDef1, 1));
//    ItemDefinition itemDef2 = createItemDefinition(ItemType.TEXTFIELD, "item2", false, null);
//    FormItem item2 = formService.createFormItem(createFormItem(0, itemDef2, 2));
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
    String groupId = String.valueOf(getGroupId());
    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);

    FormItemData formItemData1 = new FormItemData(updatedItem1, "test@gmail.com");

    Application app = applicationService.applyForMembership(new ApplicationForm(formSpecification.getId(), List.of(formItemData1), FormSpecification.FormType.INITIAL), submission, "");

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.SUBMITTED;
  }

  @Test
  void approveApplication() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);

    FormItemData formItemData1 = new FormItemData(updatedItem1, "test@gmail.com");

    Application app = applicationService.applyForMembership(new ApplicationForm(formSpecification.getId(), new ArrayList<>(List.of(formItemData1)), FormSpecification.FormType.INITIAL), submission, "");

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
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);

    List<FormItemData> data = applicationService.loadForm(sessionProvider.getCurrentSession(), formSpecification, FormSpecification.FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == updatedItem1.getId();
  }

  @Test
  void loadFormCallsAfterFormItemsPrefilledHook() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.TEXTFIELD, "item1", false, null);
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule module = new AssignedFormModule("testModuleBeforeSubmission", new HashMap<>());

    formService.setModules(null, formSpecification, List.of(module));

    List<FormItemData> data = applicationService.loadForm(sessionProvider.getCurrentSession(), formSpecification, FormSpecification.FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == updatedItem1.getId();
    assert data.getFirst().getPrefilledValue().equals("testModuleValue");
  }

  @Test
  void loadFormPrefillsValues() throws Exception {
    String groupId = String.valueOf(getGroupId());
    FormSpecification formSpecification = formService.createForm(groupId);

    // Create ItemDefinition with prefill strategy from the start
    ItemDefinition itemDef1 = new ItemDefinition();
    itemDef1.setType(ItemType.TEXTFIELD);
    itemDef1.setDisplayName("item1");
    itemDef1.setRequired(false);
    itemDef1.setValidator(null);
    itemDef1.setFormTypes(Set.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION));
    itemDef1.setHidden(ItemDefinition.Condition.NEVER);
    itemDef1.setDisabled(ItemDefinition.Condition.NEVER);
    itemDef1.setGlobal(false);
    
    // Create and save prefill strategy first to get its ID
    PrefillStrategyEntry prefillStrategy = new PrefillStrategyEntry();
    prefillStrategy.setType(PrefillStrategyEntry.PrefillStrategyType.IDENTITY_ATTRIBUTE);
    prefillStrategy.setSourceAttribute("testAttribute");
    prefillStrategy.setOptions(new HashMap<>());
    prefillStrategy = formService.createPrefillStrategy(prefillStrategy);
    
    // Set the prefill strategy ID in the ItemDefinition
    itemDef1.setPrefillStrategyIds(List.of(prefillStrategy.getId()));
    itemDef1 = formService.createItemDefinition(itemDef1);
    
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);

    List<FormItemData> data = applicationService.loadForm(sessionProvider.getCurrentSession(), formSpecification, FormSpecification.FormType.INITIAL);

    assert data != null;
    assert data.size() == 1;
    assert data.getFirst().getFormItem().getId() == updatedItem1.getId();
    assert data.getFirst().getPrefilledValue().equals("testValue");
  }
}
