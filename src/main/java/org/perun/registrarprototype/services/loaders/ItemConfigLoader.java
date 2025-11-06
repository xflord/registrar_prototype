package org.perun.registrarprototype.services.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class ItemConfigLoader {

  private final ResourceLoader resourceLoader;

  public ItemConfigLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void loadItemDefinitions(String path) {
    List<ItemDefinition> itemDefinitions = new ArrayList<>();
    Map<String, Map<String, Object>> itemDefinitionMap = loadYaml(path);
    for (Map.Entry<String, Map<String, Object>> entry : itemDefinitionMap.entrySet()) {
      Map<String, Object> definitionMap = entry.getValue();
      String displayName = entry.getKey();

      ItemDefinition itemDefinition = new ItemDefinition();
      itemDefinition.setDisplayName(displayName);
      itemDefinition.setType(getEnum(definitionMap.get("type"), ItemType.class));
      List<PrefillStrategyEntry> prefillStrategies = null;
      List<Map<String, Object>> prefillStrategiesRaw = (List<Map<String, Object>>) definitionMap.get("prefillStrategies");
      if (prefillStrategiesRaw != null) {
        prefillStrategies = getPrefillStrategyEntries(prefillStrategiesRaw);
      }
      itemDefinition.setPrefillStrategies(prefillStrategies);

      itemDefinition.setDestinationAttributeUrn((String) definitionMap.get("destinationAttributeUrn"));
      itemDefinition.setFormTypes(parseEnumSet(definitionMap.get("formTypes"), FormSpecification.FormType.class));
      itemDefinition.setHidden(getEnum(definitionMap.get("hidden"), ItemDefinition.Condition.class));
      itemDefinition.setDisabled(getEnum(definitionMap.get("disabled"), ItemDefinition.Condition.class));
      Map<String, Map<String, String>> textsRaw =
                    (Map<String, Map<String, String>>) definitionMap.get("texts");
      Map<Locale, ItemTexts> itemTexts = null;
      if (textsRaw != null) {
        itemTexts = new HashMap<>();
        for (String key : textsRaw.keySet()) {
          Locale locale = Locale.forLanguageTag(key);
          Map<String, String> parts =  textsRaw.get(key);

          String label = parts.get("label");
          String hint =  parts.get("help");
          String error = parts.get("error");

          ItemTexts text = new ItemTexts(label, hint, error);
          itemTexts.put(locale, text);
        }
      }
      itemDefinition.setTexts(itemTexts);
      itemDefinition.setDefaultValue((String) definitionMap.get("defaultValue"));

      itemDefinition.setGlobal(true);
      itemDefinitions.add(itemDefinition);
    }
  }

  private List<PrefillStrategyEntry> getPrefillStrategyEntries(List<Map<String, Object>> prefillStrategiesRaw) {
    List<PrefillStrategyEntry> prefillStrategies;
    prefillStrategies = new ArrayList<>();
    for (Map<String, Object> prefillStrategyRaw : prefillStrategiesRaw) {
      PrefillStrategyEntry prefillStrategy = new PrefillStrategyEntry();
      prefillStrategy.setType(getEnum(prefillStrategyRaw.get("type"), PrefillStrategyEntry.PrefillStrategyType.class));
      prefillStrategy.setSourceAttribute((String) prefillStrategyRaw.get("sourceAttribute"));
      prefillStrategy.setOptions(prefillStrategyRaw.containsKey("options") ? (Map<String, String>)  prefillStrategyRaw.get("options") : null);

      prefillStrategies.add(prefillStrategy);
    }
    return prefillStrategies;
  }

  public void loadPrefillStrategies(String path) {
    List<PrefillStrategyEntry> prefillStrategies = new ArrayList<>();
    List<Map<String, Object>> prefillStrategiesMap = loadYaml(path);
    prefillStrategies = getPrefillStrategyEntries(prefillStrategiesMap);
    // TODO set `global` field?

  }

  private <T> T loadYaml(String path) {
    Yaml yaml = new Yaml();
    try (InputStream in = resourceLoader.getResource(path).getInputStream()) {
      return yaml.load(in);
    } catch (IOException e) {
      System.err.println("Failed to load attribute policy file: " + path);
      throw new RuntimeException("Failed to load attribute policy file: " + path, e);
    }
  }

  private Boolean getBool(Map<String, Object> map, String key, Boolean defaultVal) {
    Object val = map.get(key);
    if (val == null) return defaultVal;
    return Boolean.parseBoolean(val.toString());
  }

  private <E extends Enum<E>> E getEnum(Object raw, Class<E> enumClass) {
    return raw == null ? null : Enum.valueOf(enumClass, (String) raw);
  }

  private <E extends Enum<E>> Set<E> parseEnumSet(Object raw, Class<E> enumClass) {
    if (raw == null) return null;
    List<String> names = (List<String>) raw;
    EnumSet<E> set = EnumSet.noneOf(enumClass);
    for (String n : names) {
      set.add(Enum.valueOf(enumClass, n));
    }
    return set;
  }
}
