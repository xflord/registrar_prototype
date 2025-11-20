package org.perun.registrarprototype.models;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
  private Destination destination;
  private Set<FormSpecification.FormType> formTypes = Set.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION);
  // potentially extract presentation fields into another class
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private Condition hidden;
  private Condition disabled;

  private String defaultValue; // do we need this?
  private boolean global; // for now global == automatically inserted AND required? Or

  public ItemDefinition() {}

  public ItemDefinition(ItemDefinition itemDefinition) {
    this(itemDefinition.getId(), itemDefinition.getFormSpecification(), itemDefinition.getDisplayName(),
        itemDefinition.getType(), itemDefinition.getUpdatable(), itemDefinition.getRequired(),
        itemDefinition.getValidator(), itemDefinition.getPrefillStrategies(),
        itemDefinition.getDestination(), itemDefinition.getFormTypes(),
        itemDefinition.getTexts(), itemDefinition.getHidden(), itemDefinition.getDisabled(),
        itemDefinition.getDefaultValue(), itemDefinition.isGlobal());
  }

  public ItemDefinition(int id, FormSpecification formSpecification, String displayName, ItemType type, Boolean updatable, Boolean required,
                        String validator,
                        List<PrefillStrategyEntry> prefillStrategies, Destination destination,
                        Set<FormSpecification.FormType> formTypes, Map<Locale, ItemTexts> texts, Condition hidden,
                        Condition disabled, String defaultValue, boolean global) {
    this.id = id;
    this.formSpecification = formSpecification;
    this.displayName = displayName;
    this.type = type;
    this.updatable = updatable;
    this.required = required;
    this.validator = validator;
    this.prefillStrategies = prefillStrategies;
    this.destination = destination;
    this.formTypes = formTypes;
    this.texts = texts;
    this.hidden = hidden;
    this.disabled = disabled;
    this.defaultValue = defaultValue;
    this.global = global;

    this.performTypeSpecificChecks();
    this.checkFormSpecificationConsistency();
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

  public Destination getDestination() {
    return destination;
  }

  public void setDestination(Destination destination) {
    this.destination = destination;
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

  public FormSpecification getFormSpecification() {
    return formSpecification;
  }

  public void setFormSpecification(FormSpecification formSpecification) {
    this.formSpecification = formSpecification;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemDefinition that = (ItemDefinition) o;
    return getId() == that.getId() && isGlobal() == that.isGlobal() &&
               Objects.equals(getFormSpecification(), that.getFormSpecification()) &&
               Objects.equals(getDisplayName(), that.getDisplayName()) && getType() == that.getType() &&
               Objects.equals(getUpdatable(), that.getUpdatable()) &&
               Objects.equals(getRequired(), that.getRequired()) &&
               Objects.equals(getValidator(), that.getValidator()) &&
               Objects.equals(getPrefillStrategies(), that.getPrefillStrategies()) &&
               Objects.equals(getDestination(), that.getDestination()) &&
               Objects.equals(getFormTypes(), that.getFormTypes()) &&
               Objects.equals(getTexts(), that.getTexts()) && getHidden() == that.getHidden() &&
               getDisabled() == that.getDisabled() && Objects.equals(getDefaultValue(), that.getDefaultValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getFormSpecification(), getDisplayName(), getType(), getUpdatable(), getRequired(),
        getValidator(), getPrefillStrategies(), getDestination(), getFormTypes(), getTexts(), getHidden(),
        getDisabled(), getDefaultValue(), isGlobal());
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
               ", destinationAttributeUrn='" + destination + '\'' +
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

  private void performTypeSpecificChecks() {
    if (this.isUpdatable() != null && this.isUpdatable() && !this.getType().isUpdatable()) {
      throw new IllegalArgumentException("Form item " + this + " of non-updatable type cannot be updatable");
    }

    if (this.getType().isLayoutItem()) {
      if (this.isRequired()) {
        throw new IllegalArgumentException("Layout form item " + this + " cannot be required");
      }
      if (this.getDefaultValue() != null) {
        throw new IllegalArgumentException("Layout form item " + this + " cannot have a default value");
      }
      if (this.getDestination() != null) {
        throw new IllegalArgumentException("Layout form item " + this + " cannot have a destination attribute");
      }
      if (this.getPrefillStrategies() != null && !this.getPrefillStrategies().isEmpty()) {
        throw new IllegalArgumentException("Layout form item " + this + " cannot have a prefill strategy");
      }
    }


    if (this.getType().equals(ItemType.PASSWORD)) {
      // TODO a form of enforcing certain rules (label format, allowed destination attributes, etc.) via yaml config?
      if (this.getDestination() == null) {
        throw new IllegalArgumentException("Password item must have a destination IDM attribute");
      }
    }

    if (this.getType().isHtmlItem()) {
      // TODO validate/sanitize HTML content
    }
    // TODO check validators (e.g. valid regexes, etc.)
  }

  private void checkFormSpecificationConsistency() {
    if (isGlobal()) {
      if (getPrefillStrategies() != null) {
        for (PrefillStrategyEntry prefillStrategyEntry : getPrefillStrategies()) {
          if (!prefillStrategyEntry.isGlobal()) {
            throw new IllegalArgumentException("Assigning non-global prefill strategy to global item definition");
          }
        }
      }
      if (getDestination() != null && !getDestination().isGlobal()) {
        throw new IllegalArgumentException("Assigning non-global destination to global item definition");
      }
    } else {
      if (getFormSpecification() == null) {
        throw new IllegalArgumentException("Missing form specification for non-global item definition");
      }
      if (getPrefillStrategies() != null) {
        for (PrefillStrategyEntry prefillStrategyEntry : getPrefillStrategies()) {
          if (!prefillStrategyEntry.isGlobal()) {
            if (prefillStrategyEntry.getFormSpecification() == null) {
              throw new IllegalStateException("Missing form specification for non-global prefill strategy");
            }
            if (!prefillStrategyEntry.getFormSpecification().equals(getFormSpecification())) {
              throw new IllegalArgumentException("Form specification of prefill strategy: " + prefillStrategyEntry + " does not match that of the item definition");
            }
          }
        }
      }
      if (getDestination() != null && !getDestination().isGlobal() &&
              !Objects.equals(getFormSpecification(), getDestination().getFormSpecification())) {
        throw new IllegalArgumentException("Destination: " + getDestination() + " is not defined for the form specification of this item definition");
      }
    }
  }
}
