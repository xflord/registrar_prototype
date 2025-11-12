package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;

public class ItemDefinitionDTO {
  private Integer id;
  private Integer formSpecificationId;
  private String displayName;
  private ItemType type;
  private Boolean updatable;
  private Boolean required;
  private String validator;
  private List<PrefillStrategyEntryDTO> prefillStrategies;
  private String destinationAttributeUrn;
  private Set<FormSpecification.FormType> formTypes;
  private Map<String, ItemTextsDTO> texts; // Map<LocaleString, ItemTextsDTO>
  private ItemDefinition.Condition hidden;
  private ItemDefinition.Condition disabled;
  private String defaultValue;
  private boolean global;

  public ItemDefinitionDTO() {}

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
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

  public String getValidator() {
    return validator;
  }

  public void setValidator(String validator) {
    this.validator = validator;
  }

  public List<PrefillStrategyEntryDTO> getPrefillStrategies() {
    return prefillStrategies;
  }

  public void setPrefillStrategies(List<PrefillStrategyEntryDTO> prefillStrategies) {
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

  public Map<String, ItemTextsDTO> getTexts() {
    return texts;
  }

  public void setTexts(Map<String, ItemTextsDTO> texts) {
    this.texts = texts;
  }

  public ItemDefinition.Condition getHidden() {
    return hidden;
  }

  public void setHidden(ItemDefinition.Condition hidden) {
    this.hidden = hidden;
  }

  public ItemDefinition.Condition getDisabled() {
    return disabled;
  }

  public void setDisabled(ItemDefinition.Condition disabled) {
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
}

