package org.perun.registrarprototype.models;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Defines an attribute to be used either for prefilling (source attribute) or as the destination IdM attribute to save
 * the value of form item after approval.
 * Source attributes should have at least the urn and display name filled out, destination attributes will utilize all fields
 */
public class AttributePolicy {
  private int id;

  private String urn; // Attribute URN
  private String displayName;

  // --- ALLOWED USAGE RULES ---
  private boolean allowAsSource; // can be used as arbitrary source
  private FormItem.PrefillStrategyType sourcePrefillStrategy; // when using as source

  private boolean allowAsDestination;
  private Set<String> allowedSourceAttributes; // explicit attributes to use as source ( they do not have to be defined as such)
  private Set<FormItem.Type> allowedItemTypes; // e.g. LOGIN, PASSWORD
  private Set<FormItem.PrefillStrategyType> allowedPrefillStrategies;
  private List<PrefillStrategyEntry> enforcedPrefillOptions;


  // --- ENFORCED BEHAVIOR RULES ---
  private Boolean enforcedRequired;
  private Boolean enforcedUpdatable;
  private FormItem.Condition enforcedHidden;
  private FormItem.Condition enforcedDisabled;

  // --- ENFORCED LABELS & TEXTS ---
  // For each locale, a bundle of label/help/error text, similar to FormItem.ItemTexts
  private Map<Locale, ItemTexts> enforcedTexts;
  private boolean enforceLabels; // if true → form manager cannot override labels
  private boolean enforceHelp;   // if true → form manager cannot override help texts
  private boolean enforceError;  // if true → form manager cannot override error messages

  public AttributePolicy() {}

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public boolean isAllowAsSource() {
    return allowAsSource;
  }

  public void setAllowAsSource(boolean allowAsSource) {
    this.allowAsSource = allowAsSource;
  }

  public boolean isAllowAsDestination() {
    return allowAsDestination;
  }

  public void setAllowAsDestination(boolean allowAsDestination) {
    this.allowAsDestination = allowAsDestination;
  }

  public Set<FormItem.Type> getAllowedItemTypes() {
    return allowedItemTypes;
  }

  public void setAllowedItemTypes(Set<FormItem.Type> allowedItemTypes) {
    this.allowedItemTypes = allowedItemTypes;
  }

  public Set<FormItem.PrefillStrategyType> getAllowedPrefillStrategies() {
    return allowedPrefillStrategies;
  }

  public void setAllowedPrefillStrategies(
      Set<FormItem.PrefillStrategyType> allowedPrefillStrategies) {
    this.allowedPrefillStrategies = allowedPrefillStrategies;
  }

  public List<PrefillStrategyEntry> getEnforcedPrefillOptions() {
    return enforcedPrefillOptions;
  }

  public void setEnforcedPrefillOptions(List<PrefillStrategyEntry> enforcedPrefillOptions) {
    this.enforcedPrefillOptions = enforcedPrefillOptions;
  }

  public Boolean getEnforcedRequired() {
    return enforcedRequired;
  }

  public void setEnforcedRequired(Boolean enforcedRequired) {
    this.enforcedRequired = enforcedRequired;
  }

  public Boolean getEnforcedUpdatable() {
    return enforcedUpdatable;
  }

  public void setEnforcedUpdatable(Boolean enforcedUpdatable) {
    this.enforcedUpdatable = enforcedUpdatable;
  }

  public FormItem.Condition getEnforcedHidden() {
    return enforcedHidden;
  }

  public void setEnforcedHidden(FormItem.Condition enforcedHidden) {
    this.enforcedHidden = enforcedHidden;
  }

  public FormItem.Condition getEnforcedDisabled() {
    return enforcedDisabled;
  }

  public void setEnforcedDisabled(FormItem.Condition enforcedDisabled) {
    this.enforcedDisabled = enforcedDisabled;
  }

  public Map<Locale, ItemTexts> getEnforcedTexts() {
    return enforcedTexts;
  }

  public void setEnforcedTexts(Map<Locale, ItemTexts> enforcedTexts) {
    this.enforcedTexts = enforcedTexts;
  }

  public boolean isEnforceLabels() {
    return enforceLabels;
  }

  public void setEnforceLabels(boolean enforceLabels) {
    this.enforceLabels = enforceLabels;
  }

  public boolean isEnforceHelp() {
    return enforceHelp;
  }

  public void setEnforceHelp(boolean enforceHelp) {
    this.enforceHelp = enforceHelp;
  }

  public boolean isEnforceError() {
    return enforceError;
  }

  public void setEnforceError(boolean enforceError) {
    this.enforceError = enforceError;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Set<String> getAllowedSourceAttributes() {
    return allowedSourceAttributes;
  }

  public void setAllowedSourceAttributes(Set<String> allowedSourceAttributes) {
    this.allowedSourceAttributes = allowedSourceAttributes;
  }

  public FormItem.PrefillStrategyType getSourcePrefillStrategy() {
    return sourcePrefillStrategy;
  }

  public void setSourcePrefillStrategy(
      FormItem.PrefillStrategyType sourcePrefillStrategy) {
    this.sourcePrefillStrategy = sourcePrefillStrategy;
  }

  @Override
  public String toString() {
    return "AttributePolicy{" +
               "id=" + id +
               ", urn='" + urn + '\'' +
               ", displayName='" + displayName + '\'' +
               ", allowAsSource=" + allowAsSource +
               ", allowedSourcePrefillStrategies=" + sourcePrefillStrategy +
               ", allowAsDestination=" + allowAsDestination +
               ", allowedSourceAttributes=" + allowedSourceAttributes +
               ", allowedItemTypes=" + allowedItemTypes +
               ", allowedDestinationPrefillStrategies=" + allowedPrefillStrategies +
               ", enforcedPrefillOptions=" + enforcedPrefillOptions +
               ", enforcedRequired=" + enforcedRequired +
               ", enforcedUpdatable=" + enforcedUpdatable +
               ", enforcedHidden=" + enforcedHidden +
               ", enforcedDisabled=" + enforcedDisabled +
               ", enforcedTexts=" + enforcedTexts +
               ", enforceLabels=" + enforceLabels +
               ", enforceHelp=" + enforceHelp +
               ", enforceError=" + enforceError +
               '}';
  }
}
