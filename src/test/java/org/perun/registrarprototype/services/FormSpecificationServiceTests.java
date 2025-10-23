package org.perun.registrarprototype.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FormSpecificationServiceTests extends GenericRegistrarServiceTests {

  @Test
  void createFormWithoutConstraints() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(formSpecification.getId(), item1);
    FormItem item2 = new FormItem(2, FormItem.Type.TEXTFIELD);
    item2 = formService.setFormItem(formSpecification.getId(), item2);

    FormSpecification createdFormSpecification = formRepository.findById(formSpecification.getId()).orElse(null);
    assert createdFormSpecification == formSpecification;
    assert createdFormSpecification.getGroupId() == groupId;
    assert createdFormSpecification.getItems().size() == 2;
    assert createdFormSpecification.getItems().contains(item1);
    assert createdFormSpecification.getItems().contains(item2);
  }

  @Test
  void createFormCorrectConstraints() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    FormSpecification createdFormSpecification = formRepository.findById(formSpecification.getId()).orElse(null);
    assert createdFormSpecification == formSpecification;
    assert createdFormSpecification.getGroupId() == groupId;
    assert createdFormSpecification.getItems().size() == 1;
    assert createdFormSpecification.getItems().contains(item1);
  }

  @Test
  void setModulesWithoutOptions() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule module = new AssignedFormModule("testModule", new HashMap<>());

    formService.setModules(null, formSpecification.getId(), List.of(module));

    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(formSpecification.getId());

    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    assert moduleWithComponent.getModuleName().equals("testModule");
    assert moduleWithComponent.getOptions().isEmpty();
    assert moduleWithComponent.getFormId() == formSpecification.getId();
    assert moduleWithComponent.getFormModule() != null;
  }

  @Test
  void setModulesWithOptions() throws Exception {
    int groupId = getGroupId();

    FormSpecification formSpecification = formService.createForm(groupId);

    FormItem item1 = new FormItem(1, FormItem.Type.TEXTFIELD);
    item1 = formService.setFormItem(formSpecification.getId(), item1);


    AssignedFormModule module = new AssignedFormModule("testModuleWithOptions", Map.of("option1", "value1", "option2", "value2"));

    formService.setModules(null, formSpecification.getId(), List.of(module));

    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(formSpecification.getId());

    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    assert moduleWithComponent.getModuleName().equals("testModuleWithOptions");
    assert moduleWithComponent.getOptions().size() == 2;
    assert moduleWithComponent.getFormId() == formSpecification.getId();
    assert moduleWithComponent.getFormModule() != null;
  }
}
