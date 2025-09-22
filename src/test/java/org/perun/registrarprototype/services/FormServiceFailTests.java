package org.perun.registrarprototype.services;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class FormServiceFailTests extends GenericRegistrarServiceTests {

  @Autowired
  private FormModuleRepository formModuleRepository;


  @Test
  void createFormIncorrectConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    assertThrows(FormItemRegexNotValid.class, () -> formService.createForm(null, groupId, List.of(item1)));
  }

  @Test
  void setModuleMissingRequiredOptions() throws Exception {
    FormItem item1 = new FormItem(1, "test");

    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId, List.of(item1));

    AssignedFormModule
        module = new AssignedFormModule("testModuleWithOptions", new HashMap<>());

    assertThrows(IllegalArgumentException.class, () -> formService.setModules(form.getId(), List.of(module)));
  }
}
