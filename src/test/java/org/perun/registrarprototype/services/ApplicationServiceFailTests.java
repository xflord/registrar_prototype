package org.perun.registrarprototype.services;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.ApplicationForm;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationServiceFailTests extends GenericRegistrarServiceTests {


  @Test
  void applyMissingRequiredItems() throws Exception {
    String groupId = String.valueOf(getGroupId());
    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", true, "");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    item1 = formService.setFormItem(formSpecification.getId(), item1);
    
    ItemDefinition itemDef2 = createItemDefinition(ItemType.TEXTFIELD, "item2", false, null);
    FormItem item2 = createFormItem(formSpecification.getId(), itemDef2, 2);
    item2 = formService.setFormItem(formSpecification.getId(), item2);

    FormItemData formItemData1 = new FormItemData(item2, "test2");

    InvalidApplicationDataException
        ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new ApplicationForm(
        formSpecification, List.of(formItemData1), FormSpecification.FormType.INITIAL), submission, ""));
    assert ex.getErrors().getFirst().itemId() == item1.getId();
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getItemDefinition().getLabel() + " is required");
  }

  @Test
  void applyMissingRequiredItemsIncorrectConstraints() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", true, "");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    item1 = formService.setFormItem(formSpecification.getId(), item1);
    
    ItemDefinition itemDef2 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item2 = createFormItem(formSpecification.getId(), itemDef2, 2);
    item2 = formService.setFormItem(formSpecification.getId(), item2);

    FormItemData formItemData = new FormItemData(item2, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new ApplicationForm(
        formSpecification, List.of(formItemData), FormSpecification.FormType.INITIAL), submission, ""));
    assert ex.getErrors().getFirst().itemId() == item1.getId();
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getItemDefinition().getLabel() + " is required");
    assert ex.getErrors().get(1).itemId() == item2.getId();
    assert ex.getErrors().get(1).message().equals("Item " + item2.getItemDefinition().getLabel() + " must match constraint " + item2.getItemDefinition().getValidator());
  }

  @Test
  void applyWithIncorrectItemConstraints() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    FormItemData formItemData1 = new FormItemData(item1, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new ApplicationForm(
        formSpecification, List.of(formItemData1), FormSpecification.FormType.INITIAL), submission, ""));
    assert ex.getErrors().getFirst().itemId() == item1.getId();
    assert ex.getErrors().getFirst().message().equals("Item " + item1.getItemDefinition().getLabel() + " must match constraint " + item1.getItemDefinition().getValidator());
  }
//
//  @Test
//  void loadFormExistingOpenApplication() throws Exception {
//    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
//    int groupId = 1;
//    perunIntegrationService.createGroup(groupId);
//
//    Form form = formService.createForm(null, groupId, List.of(item1));
//
//    FormItemData formItemData1 = new FormItemData(item1, "test@gmail.com");
//
//    applicationService.applyForMembership(1, groupId, List.of(formItemData1));
//
//    // TODO either check error message or create more exceptions (this does not guarantee that the correct exception is thrown)
//    assertThrows(IllegalArgumentException.class, () -> applicationService.loadForm(new CurrentUser(1, null), form, FormSpecification.FormType.INITIAL, List.of(item1)));
//  }
}
