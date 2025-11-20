package org.perun.registrarprototype;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.services.FormService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "development & keycloak" })
public class TestDataKeycloak {

  @Bean
  CommandLineRunner initDatabase(FormService formService) {
    return args -> {

      FormSpecification formSpecification2 = formService.createForm("0439c61e-5ef2-498c-aed0-733b65b0cc17");

      ItemTexts itemTexts2 = new ItemTexts("Full name", "Enter your full name", "Name is required");
      PrefillStrategyEntry
          prefillStrat22 = new PrefillStrategyEntry(-2, PrefillStrategyEntry.PrefillStrategyType.IDM_ATTRIBUTE, new HashMap<>(), "firstName", formSpecification2, false);
      Destination destination = new Destination(0, "firstName", null, true);
      destination = formService.createDestination(destination);
      ItemDefinition
          itemDef22 = new ItemDefinition(-2, formSpecification2, "full name", ItemType.TEXTFIELD, false, true, null,
          List.of(prefillStrat22), destination, Set.of(FormSpecification.FormType.INITIAL),
          Map.of(Locale.ENGLISH, itemTexts2), ItemDefinition.Condition.NEVER,
          ItemDefinition.Condition.IF_PREFILLED, null, false);
      itemDef22 = formService.createItemDefinition(itemDef22);
      FormItem item22 = new FormItem(-2, formSpecification2.getId(), "full name", null, 2, null, null, itemDef22);


      ItemTexts itemTexts23 = new ItemTexts("Address", "Enter your address", "Address is required");
      destination = new Destination(0, "address", null, true);
      destination = formService.createDestination(destination);
      PrefillStrategyEntry prefillStrat23 = new PrefillStrategyEntry(-2, PrefillStrategyEntry.PrefillStrategyType.IDM_ATTRIBUTE, new HashMap<>(), "address", formSpecification2, false);
      ItemDefinition itemDef23 = new ItemDefinition(-3, formSpecification2, "address", ItemType.TEXTFIELD, false, true, null,
          List.of(prefillStrat23), destination, Set.of(FormSpecification.FormType.INITIAL),
          Map.of(Locale.ENGLISH, itemTexts23), ItemDefinition.Condition.NEVER,
          ItemDefinition.Condition.NEVER, null, false);
      itemDef23 = formService.createItemDefinition(itemDef23);
      FormItem item23 = new FormItem(-2, formSpecification2.getId(), "full name", null, 3, null, null, itemDef23);

      ItemDefinition itemDef33 = new ItemDefinition(-4, formSpecification2, "submit", ItemType.SUBMIT_BUTTON, false, false, null,
          null, null, Set.of(FormSpecification.FormType.INITIAL),
          Map.of(Locale.ENGLISH, itemTexts2), ItemDefinition.Condition.NEVER,
          ItemDefinition.Condition.NEVER, null, false);
      itemDef33 = formService.createItemDefinition(itemDef33);
      FormItem submit2 = new FormItem(-3, formSpecification2.getId(), "submit", null, 4, null, null, itemDef33);

      formService.updateFormItems(formSpecification2.getId(), List.of(item22, item23, submit2));
    };
  }
}
