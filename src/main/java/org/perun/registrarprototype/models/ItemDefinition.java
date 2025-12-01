package org.perun.registrarprototype.models;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ItemDefinition {
  private int id;
  private Integer formSpecificationId; // ID of the FormSpecification this ItemDefinition belongs to

  private String displayName;
  private ItemType type;
  private Boolean updatable;
  private Boolean required;
  private String validator; // todo replace with validator abstraction
  private List<Integer> prefillStrategyIds; // IDs of PrefillStrategyEntry objects
  private Integer destinationId; // ID of the Destination this ItemDefinition is associated with
  private Set<FormSpecification.FormType> formTypes = Set.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION);
  // potentially extract presentation fields into another class
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private Condition hidden;
  private Condition disabled;

  private String defaultValue; // do we need this?
  private boolean global; // for now global == automatically inserted AND required? Or

  public ItemDefinition() {}

  public ItemDefinition(ItemDefinition itemDefinition) {
    this(itemDefinition.getId(), itemDefinition.getFormSpecificationId(), itemDefinition.getDisplayName(),
        itemDefinition.getType(), itemDefinition.getUpdatable(), itemDefinition.getRequired(),
        itemDefinition.getValidator(), itemDefinition.getPrefillStrategyIds(),
        itemDefinition.getDestinationId(), itemDefinition.getFormTypes(),
        itemDefinition.getTexts(), itemDefinition.getHidden(), itemDefinition.getDisabled(),
        itemDefinition.getDefaultValue(), itemDefinition.isGlobal());
  }

  public ItemDefinition(int id, Integer formSpecificationId, String displayName, ItemType type, Boolean updatable, Boolean required,
                        String validator,
                        List<Integer> prefillStrategyIds, Integer destinationId,
                        Set<FormSpecification.FormType> formTypes, Map<Locale, ItemTexts> texts, Condition hidden,
                        Condition disabled, String defaultValue, boolean global) {
    this.id = id;
    this.formSpecificationId = formSpecificationId;
    this.displayName = displayName;
    this.type = type;
    this.updatable = updatable;
    this.required = required;
    this.validator = validator;
    this.prefillStrategyIds = prefillStrategyIds;
    this.destinationId = destinationId;
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

  public List<Integer> getPrefillStrategyIds() {
    return prefillStrategyIds;
  }

  public void setPrefillStrategyIds(List<Integer> prefillStrategyIds) {
    this.prefillStrategyIds = prefillStrategyIds;
  }

  public Integer getDestinationId() {
    return destinationId;
  }

  public void setDestinationId(Integer destinationId) {
    this.destinationId = destinationId;
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

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemDefinition that = (ItemDefinition) o;
    return getId() == that.getId() && isGlobal() == that.isGlobal() &&
               Objects.equals(getFormSpecificationId(), that.getFormSpecificationId()) &&
               Objects.equals(getDisplayName(), that.getDisplayName()) && getType() == that.getType() &&
               Objects.equals(getUpdatable(), that.getUpdatable()) &&
               Objects.equals(getRequired(), that.getRequired()) &&
               Objects.equals(getValidator(), that.getValidator()) &&
               Objects.equals(getPrefillStrategyIds(), that.getPrefillStrategyIds()) &&
               Objects.equals(getDestinationId(), that.getDestinationId()) &&
               Objects.equals(getFormTypes(), that.getFormTypes()) &&
               Objects.equals(getTexts(), that.getTexts()) && getHidden() == that.getHidden() &&
               getDisabled() == that.getDisabled() && Objects.equals(getDefaultValue(), that.getDefaultValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getFormSpecificationId(), getDisplayName(), getType(), getUpdatable(), getRequired(),
        getValidator(), getPrefillStrategyIds(), getDestinationId(), getFormTypes(), getTexts(), getHidden(),
        getDisabled(), getDefaultValue(), isGlobal());
  }

  @Override
  public String toString() {
    return "ItemDefinition{" +
               "id=" + id +
               ", formSpecificationId=" + formSpecificationId +
               ", displayName='" + displayName + '\'' +
               ", type=" + type +
               ", updatable=" + updatable +
               ", required=" + required +
               ", validator='" + validator + '\'' +
               ", prefillStrategyIds=" + prefillStrategyIds +
               ", destinationId=" + destinationId +
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
