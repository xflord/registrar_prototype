package org.perun.registrarprototype;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.ItemType;
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
      FormSpecification formSpecification = formService.createForm("20644");

      ItemTexts itemTexts1 = new ItemTexts("Address", "Enter your address", "Address is required");
      PrefillStrategyEntry prefillStrat = new PrefillStrategyEntry(-1,
          PrefillStrategyEntry.PrefillStrategyType.IDM_ATTRIBUTE, new HashMap<>(Map.of("test", "test")), ADDRESS_ATTR_DEF_U, formSpecification, false);
      ItemDefinition itemDef1 = new ItemDefinition(-1, formSpecification, "address", ItemType.TEXTFIELD, true, false, null,
          List.of(prefillStrat), ADDRESS_ATTR_DEF_M, Set.of(FormSpecification.FormType.INITIAL),
          Map.of(Locale.ENGLISH, itemTexts1), ItemDefinition.Condition.NEVER,
          ItemDefinition.Condition.NEVER, null, false);
      itemDef1 = formService.createItemDefinition(itemDef1);
      FormItem item1 = new FormItem(-1, formSpecification.getId(), "address", null, 1, null, null, itemDef1);


      ItemTexts itemTexts2 = new ItemTexts("Full name", "Enter your full name", "Name is required");
      PrefillStrategyEntry prefillStrat2 = new PrefillStrategyEntry(-2, PrefillStrategyEntry.PrefillStrategyType.IDENTITY_ATTRIBUTE, new HashMap<>(), "name", formSpecification, false);
      ItemDefinition itemDef2 = new ItemDefinition(-2, formSpecification, "full name", ItemType.TEXTFIELD, false, true, null,
          List.of(prefillStrat2), DISPLAY_NAME_ATTR_DEF_U, Set.of(FormSpecification.FormType.INITIAL),
          Map.of(Locale.ENGLISH, itemTexts2), ItemDefinition.Condition.NEVER,
          ItemDefinition.Condition.IF_PREFILLED, null, false);
      itemDef2 = formService.createItemDefinition(itemDef2);
      FormItem item2 = new FormItem(-2, formSpecification.getId(), "full name", null, 2, null, null, itemDef2);

      ItemDefinition itemDef3 = new ItemDefinition(-3, formSpecification, "submit", ItemType.SUBMIT_BUTTON, false, false, null,
          null, null, Set.of(FormSpecification.FormType.INITIAL),
          Map.of(Locale.ENGLISH, itemTexts2), ItemDefinition.Condition.NEVER,
          ItemDefinition.Condition.NEVER, null, false);
      itemDef3 = formService.createItemDefinition(itemDef3);
      FormItem submit = new FormItem(-3, formSpecification.getId(), "submit", null, 3, null, null, itemDef3);

      formService.updateFormItems(formSpecification.getId(), List.of(item1, item2, submit));

      FormSpecification formSpecification2 = formService.createForm("0439c61e-5ef2-498c-aed0-733b65b0cc17");

      PrefillStrategyEntry prefillStrat22 = new PrefillStrategyEntry(-2, PrefillStrategyEntry.PrefillStrategyType.IDM_ATTRIBUTE, new HashMap<>(), "firstName", formSpecification2, false);
      ItemDefinition itemDef22 = new ItemDefinition(-2, formSpecification2, "full name", ItemType.TEXTFIELD, false, true, null,
          List.of(prefillStrat22), "firstName", Set.of(FormSpecification.FormType.INITIAL),
          Map.of(Locale.ENGLISH, itemTexts2), ItemDefinition.Condition.NEVER,
          ItemDefinition.Condition.IF_PREFILLED, null, false);
      itemDef22 = formService.createItemDefinition(itemDef22);
      FormItem item22 = new FormItem(-2, formSpecification2.getId(), "full name", null, 2, null, null, itemDef22);


      ItemTexts itemTexts23 = new ItemTexts("Address", "Enter your address", "Address is required");
      PrefillStrategyEntry prefillStrat23 = new PrefillStrategyEntry(-2, PrefillStrategyEntry.PrefillStrategyType.IDM_ATTRIBUTE, new HashMap<>(), "address", formSpecification2, false);
      ItemDefinition itemDef23 = new ItemDefinition(-3, formSpecification2, "address", ItemType.TEXTFIELD, false, true, null,
          List.of(prefillStrat23), "address", Set.of(FormSpecification.FormType.INITIAL),
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
