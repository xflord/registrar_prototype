package org.perun.registrarprototype.services;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FormServiceServiceTests extends GenericRegistrarServiceTests {


  @Test
  void createFormWithoutConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "test");
    FormItem item2 = new FormItem(2, "test");

    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId, List.of(item1, item2));

    Form createdForm = formRepository.findById(form.getId()).orElse(null);
    assert createdForm == form;
    assert createdForm.getGroupId() == groupId;
    assert createdForm.getItems().size() == 2;
    assert createdForm.getItems().contains(item1);
    assert createdForm.getItems().contains(item2);
  }

  @Test
  void createFormCorrectConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId, List.of(item1));

    Form createdForm = formRepository.findById(form.getId()).orElse(null);
    assert createdForm == form;
    assert createdForm.getGroupId() == groupId;
    assert createdForm.getItems().size() == 1;
    assert createdForm.getItems().contains(item1);
  }
}
