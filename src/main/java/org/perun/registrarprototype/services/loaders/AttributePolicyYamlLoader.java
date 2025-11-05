package org.perun.registrarprototype.services.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.AttributePolicy;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class AttributePolicyYamlLoader {

  private final ResourceLoader resourceLoader;

  public AttributePolicyYamlLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public List<AttributePolicy> load(String path) {
    List<AttributePolicy> attributePolicies = new ArrayList<>();
    Map<String, Map<String, Object>> attributePolicyMap = loadYaml(path);
    for (Map.Entry<String, Map<String, Object>> entry : attributePolicyMap.entrySet()) {
      Map<String, Object> policyMap = entry.getValue();
      String urn = entry.getKey();

      AttributePolicy attributePolicy = new AttributePolicy();
      attributePolicy.setUrn(urn);
      attributePolicy.setDisplayName((String) policyMap.get("displayName"));

      attributePolicy.setAllowAsDestination(getBool(policyMap, "allowAsDestination", false));
      Set<String> allowedSourceAttributes = policyMap.get("allowedSourceAttributes") == null ? new HashSet<>() :
                                                 new HashSet<>((List<String>) policyMap.get("allowedSourceAttributes"));
      attributePolicy.setAllowedSourceAttributes(allowedSourceAttributes);
      attributePolicy.setAllowAsSource(getBool(policyMap, "allowAsSource", false));
      if (attributePolicy.isAllowAsSource()) {
        attributePolicy.setSourcePrefillStrategy(policyMap.get("sourcePrefillStrategy") == null ? null :
                                                     FormItem.PrefillStrategyType.valueOf((String) policyMap.get("sourcePrefillStrategy")));
      }

      attributePolicy.setAllowedItemTypes(parseEnumSet(policyMap.get("allowedItemTypes"), FormItem.Type.class));
      attributePolicy.setAllowedPrefillStrategies(parseEnumSet(policyMap.get("allowedPrefillStrategies"),
          FormItem.PrefillStrategyType.class));
      List<Map<String, Object>> enforcedPrefillOptionsRaw = (List<Map<String, Object>>)  policyMap.get("enforcedPrefillOptions");
      if (enforcedPrefillOptionsRaw != null) {
        List<PrefillStrategyEntry> entries = new ArrayList<>();
        for (Map<String, Object> optionEntry : enforcedPrefillOptionsRaw) {
          FormItem.PrefillStrategyType prefillStrategyType = FormItem.PrefillStrategyType.valueOf((String) optionEntry.get("prefillStrategyType"));
          String sourceAttribute = (String) optionEntry.get("sourceAttribute");
          Map<String, String> options = optionEntry.get("options") == null ? new HashMap<>() :
                                            (Map<String, String>) optionEntry.get("options");
          entries.add(new PrefillStrategyEntry(prefillStrategyType, options, sourceAttribute));
        }
        attributePolicy.setEnforcedPrefillOptions(entries);
      }

      attributePolicy.setEnforcedRequired(getBool(policyMap, "enforcedRequired", null));
      attributePolicy.setEnforcedUpdatable(getBool(policyMap, "enforcedUpdatable", null));

      attributePolicy.setEnforcedHidden(getCondition(policyMap, "enforcedHidden"));
      attributePolicy.setEnforcedDisabled(getCondition(policyMap, "enforcedDisabled"));

      attributePolicy.setEnforceLabels(getBool(policyMap, "enforceLabels", false));
      attributePolicy.setEnforceHelp(getBool(policyMap, "enforceHelp", false));
      attributePolicy.setEnforceError(getBool(policyMap, "enforceError", false));

      Map<String, Map<String, String>> enforcedTextsRaw =
                    (Map<String, Map<String, String>>) policyMap.get("enforcedTexts");
      Map<Locale, ItemTexts> itemTexts = new HashMap<>();
      if (enforcedTextsRaw != null) {
        for (String key : enforcedTextsRaw.keySet()) {
          Locale locale = Locale.forLanguageTag(key);
          Map<String, String> parts =  enforcedTextsRaw.get(key);

          String label = parts.get("label");
          String hint =  parts.get("help");
          String error = parts.get("error");

          ItemTexts text = new ItemTexts(label, hint, error);
          itemTexts.put(locale, text);
        }
      }
      attributePolicy.setEnforcedTexts(itemTexts);

      System.out.println("Loaded attribute policy: " + attributePolicy + " from yaml config.");
      attributePolicies.add(attributePolicy);
    }
    return attributePolicies;
  }

  private Map<String, Map<String, Object>> loadYaml(String path) {
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

  private FormItem.Condition getCondition(Map<String, Object> map, String key) {
    Object val = map.get(key);
    return val == null ? null : FormItem.Condition.valueOf((String) val);
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
