package org.perun.registrarprototype;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.services.ApplicationServiceImpl;
import org.perun.registrarprototype.services.FormServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("development")
public class TestData {
  private static final String ADDRESS_ATTR_DEF_M = "urn:perun:member:attribute-def:def:address";
  private static final String ADDRESS_ATTR_DEF_U = "urn:perun:user:attribute-def:def:address";

  @Bean
  CommandLineRunner initDatabase(FormServiceImpl formService, ApplicationServiceImpl applicationService) {
    return args -> {
      ItemTexts itemTexts = new ItemTexts("Address", "Enter your address", "Address is required");
      FormItem item1 = new FormItem(0, 0, FormItem.Type.TEXTFIELD, new HashMap<>(Map.of(Locale.ENGLISH, itemTexts)), false,
          null, null, ADDRESS_ATTR_DEF_U, ADDRESS_ATTR_DEF_M, false, null, List.of(Form.FormType.INITIAL), FormItem.Condition.NEVER,
          FormItem.Condition.NEVER, null, null);
      item1 = formService.createFormItem(item1);
      Form form = formService.createForm(20644, List.of(item1));
    };
  }
}
