package org.perun.registrarprototype.core.services;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.core.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.core.models.AssignedFormModule;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.core.models.FormItem;
import org.perun.registrarprototype.extension.dto.ItemType;

public class FormServiceFailTests extends GenericRegistrarServiceTests {

  @Test
  void createFormIncorrectConstraints() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);


    FormItem item1 = formService.createFormItem(new FormItem(1, ItemType.EMAIL, "email", false, "^[a-zA-Z0-9._%+-+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"));


    assertThrows(FormItemRegexNotValid.class, () -> formService.setFormItem(form.getId(), item1));
  }

  @Test
  void setModuleMissingRequiredOptions() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);

    AssignedFormModule
        module = new AssignedFormModule("testModuleWithOptions", new HashMap<>());

    assertThrows(IllegalArgumentException.class, () -> formModuleService.setModules(null, form, List.of(module)));
  }

  @Test
  void setModuleWrongRequiredOptions() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);


    FormItem item1 = new FormItem(1, ItemType.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);

    AssignedFormModule
        module = new AssignedFormModule("testModuleWithOptions", Map.of("wrongOption", "value1"));

    assertThrows(IllegalArgumentException.class, () -> formModuleService.setModules(null, form, List.of(module)));
  }

}
