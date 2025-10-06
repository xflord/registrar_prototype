package org.perun.registrarprototype.services;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.ApplicationContext;
import org.perun.registrarprototype.security.CurrentUser;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationServiceFailTests extends GenericRegistrarServiceTests {


  @Test
  void applyMissingRequiredItems() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);
    Form form = formService.createForm(null, groupId);


    FormItem item1 = new FormItem(1, FormItem.Type.EMAIL, "email", true, "");
    item1 = formService.setFormItem(form.getId(), item1);
    FormItem item2 = new FormItem(2, FormItem.Type.TEXTFIELD);
    item2 = formService.setFormItem(form.getId(), item2);

    FormItemData formItemData1 = new FormItemData(item2, "test2");

    InvalidApplicationDataException
        ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new CurrentUser(-1, null), new ApplicationContext(form, groupId, List.of(formItemData1), Form.FormType.INITIAL)));
    assert ex.getErrors().getFirst().itemId() == item1.getId();
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getLabel() + " is required");
  }

  @Test
  void applyMissingRequiredItemsIncorrectConstraints() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);


    FormItem item1 = new FormItem(1, FormItem.Type.EMAIL, "email", true, "");
    item1 = formService.setFormItem(form.getId(), item1);
    FormItem item2 = new FormItem(2, FormItem.Type.EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item2 = formService.setFormItem(form.getId(), item2);

    FormItemData formItemData = new FormItemData(item2, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new CurrentUser(-1, null), new ApplicationContext(form, groupId, List.of(formItemData), Form.FormType.INITIAL)));
    assert ex.getErrors().getFirst().itemId() == item1.getId();
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getLabel() + " is required");
    assert ex.getErrors().get(1).itemId() == item2.getId();
    assert ex.getErrors().get(1).message().equals("Item " + item2.getLabel() + " must match constraint " + item2.getConstraint());
  }

  @Test
  void applyWithIncorrectItemConstraints() throws Exception {
    int groupId = getGroupId();
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId);


    FormItem item1 = new FormItem(1, FormItem.Type.EMAIL, "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    item1 = formService.setFormItem(form.getId(), item1);

    FormItemData formItemData1 = new FormItemData(item1, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new CurrentUser(-1, null), new ApplicationContext(form, groupId, List.of(formItemData1), Form.FormType.INITIAL)));
    assert ex.getErrors().getFirst().itemId() == item1.getId();
    assert ex.getErrors().getFirst().message().equals("Item " + item1.getLabel() + " must match constraint " + item1.getConstraint());
  }
//
//  @Test
//  void loadFormExistingOpenApplication() throws Exception {
//    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
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
//    assertThrows(IllegalArgumentException.class, () -> applicationService.loadForm(new CurrentUser(1, null), form, Form.FormType.INITIAL, List.of(item1)));
//  }
}
