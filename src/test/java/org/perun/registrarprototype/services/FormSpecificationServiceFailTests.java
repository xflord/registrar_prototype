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

public class FormSpecificationServiceFailTests extends GenericRegistrarServiceTests {

  @Test
  void createFormIncorrectConstraints() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);


    FormItem item1 = formService.createFormItem(new FormItem(1, FormItem.Type.VALIDATED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"));


    assertThrows(FormItemRegexNotValid.class, () -> formService.setFormItem(formSpecification.getId(), item1));
  }

  @Test
  void setModuleMissingRequiredOptions() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule
        module = new AssignedFormModule("testModuleWithOptions", new HashMap<>());

    assertThrows(IllegalArgumentException.class, () -> formService.setModules(null, formSpecification.getId(), List.of(module)));
  }

  @Test
  void setModuleWrongRequiredOptions() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);


    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule
        module = new AssignedFormModule("testModuleWithOptions", Map.of("wrongOption", "value1"));

    assertThrows(IllegalArgumentException.class, () -> formService.setModules(null, formSpecification.getId(), List.of(module)));
  }

}
