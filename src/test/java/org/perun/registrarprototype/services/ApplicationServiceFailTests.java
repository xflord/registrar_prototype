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
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);
    
    ItemDefinition itemDef2 = createItemDefinition(ItemType.TEXTFIELD, "item2", false, null);
    FormItem item2 = createFormItem(formSpecification.getId(), itemDef2, 2);
    FormItem updatedItem2 = formService.setFormItem(formSpecification.getId(), item2);

    FormItemData formItemData1 = new FormItemData(updatedItem2, "test2");

    InvalidApplicationDataException
        ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new ApplicationForm(
        formSpecification.getId(), List.of(formItemData1), FormSpecification.FormType.INITIAL), submission, ""));
    assert ex.getErrors().getFirst().itemId() == updatedItem1.getId();
    ItemDefinition itemDefinition = itemDefinitionRepository.findById(updatedItem1.getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for ID: " + updatedItem1.getItemDefinitionId()));
    assert ex.getErrors().getFirst().message().equals("Field " + itemDefinition.getLabel() + " is required");
  }

  @Test
  void applyMissingRequiredItemsIncorrectConstraints() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", true, "");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);
    
    ItemDefinition itemDef2 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item2 = createFormItem(formSpecification.getId(), itemDef2, 2);
    FormItem updatedItem2 = formService.setFormItem(formSpecification.getId(), item2);

    FormItemData formItemData = new FormItemData(updatedItem2, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new ApplicationForm(
        formSpecification.getId(), List.of(formItemData), FormSpecification.FormType.INITIAL), submission, ""));
    assert ex.getErrors().getFirst().itemId() == updatedItem1.getId();
    
    ItemDefinition itemDefinition1 = itemDefinitionRepository.findById(updatedItem1.getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for ID: " + updatedItem1.getItemDefinitionId()));
    assert ex.getErrors().getFirst().message().equals("Field " + itemDefinition1.getLabel() + " is required");
    
    assert ex.getErrors().get(1).itemId() == updatedItem2.getId();
    
    ItemDefinition itemDefinition2 = itemDefinitionRepository.findById(updatedItem2.getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for ID: " + updatedItem2.getItemDefinitionId()));
    assert ex.getErrors().get(1).message().equals("Item " + itemDefinition2.getLabel() + " must match constraint " + itemDefinition2.getValidator());
  }

  @Test
  void applyWithIncorrectItemConstraints() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    FormItem updatedItem1 = formService.setFormItem(formSpecification.getId(), item1);

    FormItemData formItemData1 = new FormItemData(updatedItem1, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new ApplicationForm(
        formSpecification.getId(), List.of(formItemData1), FormSpecification.FormType.INITIAL), submission, ""));
    assert ex.getErrors().getFirst().itemId() == updatedItem1.getId();
    
    ItemDefinition itemDefinition1 = itemDefinitionRepository.findById(updatedItem1.getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for ID: " + updatedItem1.getItemDefinitionId()));
    assert ex.getErrors().getFirst().message().equals("Item " + itemDefinition1.getLabel() + " must match constraint " + itemDefinition1.getValidator());
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
