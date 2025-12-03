package org.perun.registrarprototype.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.services.modules.ModulesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FormSpecificationServiceTests extends GenericRegistrarServiceTests {

  @Autowired
  private ModulesManager modulesManager;

  @Override
  @BeforeEach
  void setUp() {
    super.setUp();
    // Ensure ModulesManager is initialized
    modulesManager.init();
    System.out.println("Loaded modules: " + modulesManager.getLoadedModules().keySet());
  }

  @Test
  void createFormWithoutConstraints() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.TEXTFIELD, "item1", false, null);
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    formService.setFormItem(formSpecification.getId(), item1);
    
    ItemDefinition itemDef2 = createItemDefinition(ItemType.TEXTFIELD, "item2", false, null);
    FormItem item2 = createFormItem(formSpecification.getId(), itemDef2, 2);
    formService.setFormItem(formSpecification.getId(), item2);

    FormSpecification createdFormSpecification = formRepository.findById(formSpecification.getId()).orElse(null);
    assert createdFormSpecification.getId() == formSpecification.getId();
    assert createdFormSpecification.getGroupId().equals(groupId);
    // Load items explicitly since they're not automatically loaded with the form specification in JPA implementation
    List<FormItem> formItems = formItemRepository.getFormItemsByFormId(formSpecification.getId());
    assert formItems.size() == 2;
    // Note: We can't directly compare the items because they might have different object references
    // Instead, we'll check if the items have the expected properties
    assert formItems.stream().anyMatch(item -> item.getShortName().equals("item1"));
    assert formItems.stream().anyMatch(item -> item.getShortName().equals("item2"));
  }

  @Test
  void createFormCorrectConstraints() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.VERIFIED_EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    formService.setFormItem(formSpecification.getId(), item1);

    FormSpecification createdFormSpecification = formRepository.findById(formSpecification.getId()).orElse(null);
    assert createdFormSpecification.getId() == formSpecification.getId();
    assert createdFormSpecification.getGroupId().equals(groupId);
    // Load items explicitly since they're not automatically loaded with the form specification in JPA implementation
    List<FormItem> formItems = formItemRepository.getFormItemsByFormId(formSpecification.getId());
    assert formItems.size() == 1;
    // Check if the item has the expected properties
    assert formItems.stream().anyMatch(item -> item.getShortName().equals("email"));
  }

  @Test
  void setModulesWithoutOptions() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.TEXTFIELD, "item1", false, null);
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    formService.setFormItem(formSpecification.getId(), item1);

    AssignedFormModule module = new AssignedFormModule("testModule", new HashMap<>());

    formService.setModules(null, formSpecification, List.of(module));

    List<AssignedFormModule> modules = formService.getAssignedFormModules(formSpecification);

    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    assert moduleWithComponent.getModuleName().equals("testModule");
    assert moduleWithComponent.getOptions().isEmpty();
    assert moduleWithComponent.getFormId() == formSpecification.getId();
    assert moduleWithComponent.getFormModule() != null;
  }

  @Test
  void setModulesWithOptions() throws Exception {
    String groupId = String.valueOf(getGroupId());

    FormSpecification formSpecification = formService.createForm(groupId);

    ItemDefinition itemDef1 = createItemDefinition(ItemType.TEXTFIELD, "item1", false, null);
    FormItem item1 = createFormItem(formSpecification.getId(), itemDef1, 1);
    formService.setFormItem(formSpecification.getId(), item1);


    AssignedFormModule module = new AssignedFormModule("testModuleWithOptions", Map.of("option1", "value1", "option2", "value2"));

    formService.setModules(null, formSpecification, List.of(module));

    List<AssignedFormModule> modules = formService.getAssignedFormModules(formSpecification);

    System.out.println("Modules size: " + modules.size());
    assert modules.size() == 1;

    AssignedFormModule moduleWithComponent = modules.getFirst();
    System.out.println("Module name: " + moduleWithComponent.getModuleName());
    System.out.println("Form module: " + moduleWithComponent.getFormModule());
    System.out.println("Module from manager: " + modulesManager.getModule("testModuleWithOptions"));
    
    System.out.println("Checking module name assertion...");
    assert moduleWithComponent.getModuleName().equals("testModuleWithOptions");
    System.out.println("Checking options size assertion...");
    System.out.println("Options size: " + moduleWithComponent.getOptions().size());
    assert moduleWithComponent.getOptions().size() == 2;
    System.out.println("Checking form ID assertion...");
    System.out.println("Form ID: " + moduleWithComponent.getFormId());
    System.out.println("Form specification ID: " + formSpecification.getId());
    assert moduleWithComponent.getFormId() == formSpecification.getId();
    System.out.println("Checking form module not null assertion...");
    assert moduleWithComponent.getFormModule() != null;
  }
}
