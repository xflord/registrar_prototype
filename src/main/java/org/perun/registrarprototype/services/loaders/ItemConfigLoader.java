package org.perun.registrarprototype.services.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.repositories.DestinationRepository;
import org.perun.registrarprototype.repositories.ItemDefinitionRepository;
import org.perun.registrarprototype.repositories.PrefillStrategyEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class ItemConfigLoader implements CommandLineRunner {

  private final ResourceLoader resourceLoader;

  @Value("${item-config.yaml.path}")
  private String configPath;
  private ItemDefinitionRepository itemDefinitionRepository;
  private PrefillStrategyEntryRepository prefillStrategyEntryRepository;
  private DestinationRepository destinationRepository;

  public ItemConfigLoader(ResourceLoader resourceLoader,
                          ItemDefinitionRepository itemDefinitionRepository,
                          PrefillStrategyEntryRepository prefillStrategyEntryRepository,
                          DestinationRepository destinationRepository) {
    this.resourceLoader = resourceLoader;
    this.itemDefinitionRepository = itemDefinitionRepository;
    this.prefillStrategyEntryRepository = prefillStrategyEntryRepository;
    this.destinationRepository = destinationRepository;
  }


  @Override
  public void run(String... args) throws Exception {
    System.out.println("Loading config from " + configPath);
    load();
    System.out.println("Finished loading config from " + configPath);
  }

  // TODO transactional
  public void load() {
    Map<String, Object> config = loadYaml(configPath);

    loadDestinations((List<String>) config.get("destinations"));
    loadPrefillStrategies((List<Map<String, Object>>) config.get("strategies"));
    loadItemDefinitions((Map<String, Map<String, Object>>) config.get("definitions"));
  }

  public void loadItemDefinitions(Map<String, Map<String, Object>> itemDefinitionMap) {
    if (itemDefinitionMap == null) {
      return;
    }

    List<ItemDefinition> itemDefinitions = new ArrayList<>();
    for (Map.Entry<String, Map<String, Object>> entry : itemDefinitionMap.entrySet()) {
      Map<String, Object> definitionMap = entry.getValue();
      String displayName = entry.getKey();

      ItemType type = getEnum(definitionMap.get("type"), ItemType.class);
      Boolean updatable = getBool(definitionMap, "updatable", null);
      Boolean required = getBool(definitionMap, "required", null);
      String validator = (String) definitionMap.get("validator");
      List<PrefillStrategyEntry> prefillStrategies = null;
      List<Map<String, Object>> prefillStrategiesRaw = (List<Map<String, Object>>) definitionMap.get("prefillStrategies");
      if (prefillStrategiesRaw != null) {
        prefillStrategies = getPrefillStrategyEntries(prefillStrategiesRaw);
      }

      String destinationUrn = (String) definitionMap.get("destinationAttributeUrn");
      Destination destination = null;
      if (destinationUrn != null) {
        destination = new Destination(destinationUrn, null, true);
        if (!destinationRepository.exists(destination)) {
          destinationRepository.createDestination(destination);
        }
      }

      Set<FormSpecification.FormType> formTypes = parseEnumSet(definitionMap.get("formTypes"), FormSpecification.FormType.class);
      ItemDefinition.Condition hidden = (getEnum(definitionMap.get("hidden"), ItemDefinition.Condition.class));
      ItemDefinition.Condition disabled = (getEnum(definitionMap.get("disabled"), ItemDefinition.Condition.class));
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
      String defaultValue = (String) definitionMap.get("defaultValue");


      itemDefinitions.add(new ItemDefinition(-1, null, displayName, type, updatable, required, validator,
          prefillStrategies, destination, formTypes, itemTexts, hidden, disabled, defaultValue, true));
    }

    itemDefinitionRepository.saveAll(itemDefinitions);
  }

  private List<PrefillStrategyEntry> getPrefillStrategyEntries(List<Map<String, Object>> prefillStrategiesRaw) {
    List<PrefillStrategyEntry> prefillStrategies = new ArrayList<>();
    List<PrefillStrategyEntry> entriesToSave = new ArrayList<>();
    for (Map<String, Object> prefillStrategyRaw : prefillStrategiesRaw) {
      PrefillStrategyEntry.PrefillStrategyType type = getEnum(prefillStrategyRaw.get("type"), PrefillStrategyEntry.PrefillStrategyType.class);
      String sourceAttr = (String) prefillStrategyRaw.get("sourceAttribute");
      Map<String, String> options = (prefillStrategyRaw.containsKey("options") ? (Map<String, String>)  prefillStrategyRaw.get("options") : new HashMap<>());
      PrefillStrategyEntry prefillStrategy = new PrefillStrategyEntry(-1, type, options, sourceAttr, null, true);

      Optional<PrefillStrategyEntry> existing = prefillStrategyEntryRepository.exists(prefillStrategy);
      if (existing.isPresent()) {
        System.out.println("YAML LOADER: Skipping prefill strategy " + prefillStrategy + " because it already exists");
        prefillStrategy =  existing.get();
      } else {
        entriesToSave.add(prefillStrategy);
      }

      prefillStrategies.add(prefillStrategy);
    }

    prefillStrategyEntryRepository.saveAll(entriesToSave);
    return prefillStrategies;
  }

  public void loadPrefillStrategies(List<Map<String, Object>> prefillStrategiesMap) {
    if (prefillStrategiesMap == null) {
      return;
    }

    getPrefillStrategyEntries(prefillStrategiesMap);
  }

  public void loadDestinations(List<String> destinations) {
    if (destinations == null) {
      return;
    }

    List<Destination> destinationList = new ArrayList<>();
    destinations.forEach(destinationUrn -> {
      Destination destination = new Destination(destinationUrn, null, true);
      if (!destinationRepository.exists(destination)) {
        destinationList.add(destination);
      }
    });
    destinationRepository.saveAll(destinationList);
  }

  private <T> T loadYaml(String path) {
    Yaml yaml = new Yaml();
    try (InputStream in = resourceLoader.getResource(path).getInputStream()) {
      return yaml.load(in);
    } catch (IOException e) {
      System.err.println("Failed to load item config file: " + path);
      throw new RuntimeException("Failed to load config file: " + path, e);
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
