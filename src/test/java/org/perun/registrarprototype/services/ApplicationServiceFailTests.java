package org.perun.registrarprototype.services;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.security.CurrentUser;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationServiceFailTests extends GenericRegistrarServiceTests {


  @Test
  void applyMissingRequiredItems() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", true, "");
    FormItem item2 = new FormItem(2, "test");

    int groupId = 1;
    perunIntegrationService.createGroup(groupId);
    formService.createForm(null, groupId, List.of(item1, item2));

    FormItemData formItemData1 = new FormItemData(item2, "test2");

    InvalidApplicationDataException
        ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new CurrentUser(-1, null), groupId, Form.FormType.INITIAL, List.of(formItemData1)));
    assert ex.getErrors().getFirst().itemId() == 1;
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getLabel() + " is required");
  }

  @Test
  void applyMissingRequiredItemsIncorrectConstraints() throws Exception {

    FormItem item1 = new FormItem(1, "email", "email", true, "");
    FormItem item2 = new FormItem(2, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1, item2));

    FormItemData formItemData = new FormItemData(item2, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new CurrentUser(-1, null), groupId, Form.FormType.INITIAL, List.of(formItemData)));
    assert ex.getErrors().getFirst().itemId() == 1;
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getLabel() + " is required");
    assert ex.getErrors().get(1).itemId() == 2;
    assert ex.getErrors().get(1).message().equals("Item " + item2.getLabel() + " must match constraint " + item2.getConstraint());
  }

  @Test
  void applyWithIncorrectItemConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1));

    FormItemData formItemData1 = new FormItemData(item1, "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(new CurrentUser(-1, null), groupId, Form.FormType.INITIAL, List.of(formItemData1)));
    assert ex.getErrors().getFirst().itemId() == 1;
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
