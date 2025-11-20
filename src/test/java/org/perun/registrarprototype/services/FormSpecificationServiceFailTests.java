package org.perun.registrarprototype.services;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;

public class FormSpecificationServiceFailTests extends GenericRegistrarServiceTests {

  @Test
  void createFormIncorrectConstraints() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    // Create ItemDefinition with invalid regex - this should fail when setting the form item
    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);

    assertThrows(FormItemRegexNotValid.class, () -> formService.setFormItem(formSpecification.getId(), item1));
  }

  @Test
  void setModuleMissingRequiredOptions() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.TEXTFIELD, "item1", false, null);
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule
        module = new AssignedFormModule("testModuleWithOptions", new HashMap<>());

    assertThrows(IllegalArgumentException.class, () -> formService.setModules(null, formSpecification, List.of(module)));
  }

  @Test
  void setModuleWrongRequiredOptions() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.TEXTFIELD, "item1", false, null);
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule
        module = new AssignedFormModule("testModuleWithOptions", Map.of("wrongOption", "value1"));

    assertThrows(IllegalArgumentException.class, () -> formService.setModules(null, formSpecification, List.of(module)));
  }

}
