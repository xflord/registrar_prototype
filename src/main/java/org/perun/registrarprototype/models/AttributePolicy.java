package org.perun.registrarprototype.models;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AttributePolicy {
  private String urn; // Attribute URN
  private String displayName;

  // --- ALLOWED USAGE RULES ---
  private boolean allowAsSource;
  private boolean allowAsDestination;
  private Set<FormItem.Type> allowedItemTypes; // e.g. LOGIN, PASSWORD
  private boolean allowAnyItemType;

  private Set<FormItem.PrefillStrategyType> allowedPrefillStrategies;
  private boolean allowAnyPrefillStrategy;

  // --- ENFORCED BEHAVIOR RULES ---
  private Boolean enforcedRequired;
  private Boolean enforcedUpdatable;
  private Boolean enforcedHidden;
  private Boolean enforcedDisabled;

  // --- ENFORCED LABELS & TEXTS ---
  // For each locale, a bundle of label/help/error text, similar to FormItem.ItemTexts
  private Map<Locale, ItemTexts> enforcedTexts;
  private boolean enforceLabels; // if true → form manager cannot override labels
  private boolean enforceHelp;   // if true → form manager cannot override help texts
  private boolean enforceError;  // if true → form manager cannot override error messages

  // --- METADATA ---
  private Map<String, Object> extraProperties;

  // getters/setters omitted for brevity
}
