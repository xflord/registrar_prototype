package org.perun.registrarprototype.core.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.core.models.AssignedFormModule;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.core.models.FormItem;
import org.perun.registrarprototype.extension.dto.ItemType;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FormServiceTests extends GenericRegistrarServiceTests {

  @Test
  void createFormWithoutConstraints() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);
    FormItem item2 = new FormItem(2, ItemType.TEXTFIELD);
    item2 = formService.setFormItem(form.getId(), item2);

    Form createdForm = formRepository.findById(form.getId()).orElse(null);
    assert createdForm == form;
    assert createdForm.getGroupId() == groupId;
    assert createdForm.getItems().size() == 2;
    assert createdForm.getItems().contains(item1);
    assert createdForm.getItems().contains(item2);
  }

  @Test
  void createFormCorrectConstraints() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(form.getId(), item1);

    Form createdForm = formRepository.findById(form.getId()).orElse(null);
    assert createdForm == form;
    assert createdForm.getGroupId() == groupId;
    assert createdForm.getItems().size() == 1;
    assert createdForm.getItems().contains(item1);
  }

  @Test
  void setModulesWithoutOptions() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);

    AssignedFormModule module = new AssignedFormModule("TestModule", new HashMap<>());

    formModuleService.setModules(null, form, List.of(module));

    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(form.getId());

    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    assert moduleWithComponent.getModuleName().equals("TestModule");
    assert moduleWithComponent.getOptions().isEmpty();
    assert moduleWithComponent.getFormId() == form.getId();
    assert moduleWithComponent.getFormModule() != null;
  }

  @Test
  void setModulesWithOptions() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, ItemType.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);


    AssignedFormModule module = new AssignedFormModule("TestModuleWithOptions", Map.of("option1", "value1", "option2", "value2"));

    formModuleService.setModules(null, form, List.of(module));

    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(form.getId());

    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    assert moduleWithComponent.getModuleName().equals("TestModuleWithOptions");
    assert moduleWithComponent.getOptions().size() == 2;
    assert moduleWithComponent.getFormId() == form.getId();
    assert moduleWithComponent.getFormModule() != null;
  }
}
