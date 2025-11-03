package org.perun.registrarprototype;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.services.ApplicationService;
import org.perun.registrarprototype.services.FormService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("development")
public class TestData {
  private static final String ADDRESS_ATTR_DEF_M = "urn:perun:member:attribute-def:def:address";
  private static final String ADDRESS_ATTR_DEF_U = "urn:perun:user:attribute-def:def:address";
  private static final String DISPLAY_NAME_ATTR_DEF_U = "urn:perun:user:attribute-def:core:displayName";


  @Bean
  CommandLineRunner initDatabase(FormService formService, ApplicationService applicationService) {
    return args -> {
      ItemTexts itemTexts1 = new ItemTexts("Address", "Enter your address", "Address is required");
      FormItem item1 = new FormItem(0, 0, "address", null, 1, FormItem.Type.TEXTFIELD, new HashMap<>(Map.of(Locale.ENGLISH, itemTexts1)), true, false,
          null, ADDRESS_ATTR_DEF_M, null, List.of(FormSpecification.FormType.INITIAL), FormItem.Condition.NEVER,
          FormItem.Condition.NEVER, null, null);
      item1.addPrefillStrategyEntry(new PrefillStrategyEntry(
          FormItem.PrefillStrategyType.IDM_ATTRIBUTE, Map.of("sourceAttribute", ADDRESS_ATTR_DEF_U)));
      item1 = formService.createFormItem(item1);

      ItemTexts itemTexts2 = new ItemTexts("Full name", "Enter your full name", "Name is required");
      FormItem item2 = new FormItem(0, 0, "full name", null, 1, FormItem.Type.TEXTFIELD, new HashMap<>(Map.of(Locale.ENGLISH, itemTexts2)), false, true,
          null, DISPLAY_NAME_ATTR_DEF_U, null, List.of(FormSpecification.FormType.INITIAL), FormItem.Condition.NEVER,
          FormItem.Condition.IF_PREFILLED, null, null);
      item2.addPrefillStrategyEntry(new PrefillStrategyEntry(FormItem.PrefillStrategyType.IDENTITY_ATTRIBUTE, Map.of("sourceAttribute", "name")));
      item2 = formService.createFormItem(item2);

      FormSpecification formSpecification = formService.createForm(20644, List.of(item1, item2));
    };
  }
}
