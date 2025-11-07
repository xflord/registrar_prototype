package org.perun.registrarprototype.models;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ItemDefinition {
  private int id;
  private FormSpecification formSpecification; // TODO duplicit in FormItem, decide on where to keep

  private String displayName;
  private ItemType type;
  private Boolean updatable;
  private Boolean required;
  private String validator; // todo replace with validator abstraction
  private List<PrefillStrategyEntry> prefillStrategies;
  private String destinationAttributeUrn;
  private Set<FormSpecification.FormType> formTypes = Set.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION);
  // potentially extract presentation fields into another class
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private Condition hidden;
  private Condition disabled;

  private String defaultValue; // do we need this?
  private boolean global; // for now global == automatically inserted AND required? Or

  public ItemDefinition() {}

  public ItemDefinition(ItemDefinition itemDefinition) {
    this.id = itemDefinition.getId();
    this.displayName = itemDefinition.getDisplayName();
    this.type = itemDefinition.getType();
    this.updatable = itemDefinition.getUpdatable();
    this.required = itemDefinition.getRequired();
    this.validator = itemDefinition.getValidator();
    this.prefillStrategies = itemDefinition.getPrefillStrategies();
    this.destinationAttributeUrn = itemDefinition.getDestinationAttributeUrn();
    this.formTypes = itemDefinition.getFormTypes();
    this.texts = itemDefinition.getTexts();
    this.hidden = itemDefinition.getHidden();
    this.disabled = itemDefinition.getDisabled();
    this.defaultValue = itemDefinition.getDefaultValue();
    this.global = itemDefinition.isGlobal();
  }

  public ItemDefinition(int id, String displayName, ItemType type, Boolean updatable, Boolean required,
                        String validator,
                        List<PrefillStrategyEntry> prefillStrategies, String destinationAttributeUrn,
                        Set<FormSpecification.FormType> formTypes, Map<Locale, ItemTexts> texts, Condition hidden,
                        Condition disabled, String defaultValue, boolean global) {
    this.id = id;
    this.displayName = displayName;
    this.type = type;
    this.updatable = updatable;
    this.required = required;
    this.validator = validator;
    this.prefillStrategies = prefillStrategies;
    this.destinationAttributeUrn = destinationAttributeUrn;
    this.formTypes = formTypes;
    this.texts = texts;
    this.hidden = hidden;
    this.disabled = disabled;
    this.defaultValue = defaultValue;
    this.global = global;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public ItemType getType() {
    return type;
  }

  public void setType(ItemType type) {
    this.type = type;
  }

  public Boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }

  public Boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getValidator() {
    return validator;
  }

  public void setValidator(String validator) {
    this.validator = validator;
  }

  public List<PrefillStrategyEntry> getPrefillStrategies() {
    return prefillStrategies;
  }

  public void setPrefillStrategies(List<PrefillStrategyEntry> prefillStrategies) {
    this.prefillStrategies = prefillStrategies;
  }

  public String getDestinationAttributeUrn() {
    return destinationAttributeUrn;
  }

  public void setDestinationAttributeUrn(String destinationAttributeUrn) {
    this.destinationAttributeUrn = destinationAttributeUrn;
  }

  public Set<FormSpecification.FormType> getFormTypes() {
    return formTypes;
  }

  public void setFormTypes(Set<FormSpecification.FormType> formTypes) {
    this.formTypes = formTypes;
  }

  public Map<Locale, ItemTexts> getTexts() {
    return texts;
  }

  public void setTexts(Map<Locale, ItemTexts> texts) {
    this.texts = texts;
  }

  public Condition getHidden() {
    return hidden;
  }

  public void setHidden(Condition hidden) {
    this.hidden = hidden;
  }

  public Condition getDisabled() {
    return disabled;
  }

  public void setDisabled(Condition disabled) {
    this.disabled = disabled;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public boolean isGlobal() {
    return global;
  }

  public void setGlobal(boolean global) {
    this.global = global;
  }

  public String getLabel() {
    return "default";
//    return texts.get(Locale.ENGLISH).getLabel();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Boolean getUpdatable() {
    return updatable;
  }

  public void setUpdatable(Boolean updatable) {
    this.updatable = updatable;
  }

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  @Override
  public String toString() {
    return "ItemDefinition{" +
               "displayName='" + displayName + '\'' +
               ", type=" + type +
               ", updatable=" + updatable +
               ", required=" + required +
               ", validator='" + validator + '\'' +
               ", prefillStrategies=" + prefillStrategies +
               ", destinationAttributeUrn='" + destinationAttributeUrn + '\'' +
               ", formTypes=" + formTypes +
               ", texts=" + texts +
               ", hidden=" + hidden +
               ", disabled=" + disabled +
               ", defaultValue='" + defaultValue + '\'' +
               ", global=" + global +
               '}';
  }

  public enum Condition {
    NEVER, ALWAYS, IF_PREFILLED, IF_EMPTY
  }
}
