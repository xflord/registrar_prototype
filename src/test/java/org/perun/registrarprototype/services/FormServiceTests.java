package org.perun.registrarprototype.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FormServiceTests extends GenericRegistrarServiceTests {

  @Test
  void createFormWithoutConstraints() throws Exception {
    int groupId = getGroupId();

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);
    FormItem item2 = new FormItem(2, FormItem.Type.TEXTFIELD);
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

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.VALIDATED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
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

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);

    AssignedFormModule module = new AssignedFormModule("testModule", new HashMap<>());

    formService.setModules(null, form.getId(), List.of(module));

    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(form.getId());

    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    assert moduleWithComponent.getModuleName().equals("testModule");
    assert moduleWithComponent.getOptions().isEmpty();
    assert moduleWithComponent.getFormId() == form.getId();
    assert moduleWithComponent.getFormModule() != null;
  }

  @Test
  void setModulesWithOptions() throws Exception {
    int groupId = getGroupId();

    Form form = formService.createForm(null, groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(form.getId(), item1);


    AssignedFormModule module = new AssignedFormModule("testModuleWithOptions", Map.of("option1", "value1", "option2", "value2"));

    formService.setModules(null, form.getId(), List.of(module));

    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(form.getId());

    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    assert moduleWithComponent.getModuleName().equals("testModuleWithOptions");
    assert moduleWithComponent.getOptions().size() == 2;
    assert moduleWithComponent.getFormId() == form.getId();
    assert moduleWithComponent.getFormModule() != null;
  }
}
