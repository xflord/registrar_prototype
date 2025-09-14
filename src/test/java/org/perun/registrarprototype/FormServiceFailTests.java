package org.perun.registrarprototype;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.models.FormItem;

public class FormServiceFailTests extends GenericRegistrarTests {

  @Test
  void createFormIncorrectConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    assertThrows(FormItemRegexNotValid.class, () -> formService.createForm(null, groupId, List.of(item1)));
  }
}
