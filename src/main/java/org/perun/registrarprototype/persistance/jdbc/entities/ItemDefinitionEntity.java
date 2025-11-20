package org.perun.registrarprototype.persistance.jdbc.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("item_definition")
public class ItemDefinitionEntity {

  @Id
  private Integer id;

  private Integer formSpecificationId;

  private String displayName;

  private String type;

  private Boolean updatable;

  private Boolean required;

  private String validator;

  private Integer destinationId;

  private String hiddenCondition;

  private String disabledCondition;

  private String defaultValue;

  private Boolean global;

  @MappedCollection(idColumn = "item_definition_id")
  private List<ItemTextsEntity> texts = new ArrayList<>();

  @MappedCollection(idColumn = "item_definition_id")
  private Set<FormTypeRef> formTypes = new HashSet<>();

  @MappedCollection(idColumn = "item_definition_id", keyColumn = "position")
  private List<PrefillStrategyRef> prefillStrategyRefs = new ArrayList<>();

  public ItemDefinitionEntity() {
  }

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
    return type != null ? ItemType.valueOf(type) : null;
  }

  public void setType(ItemType type) {
    this.type = type != null ? type.name() : null;
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

  public Integer getDestinationId() {
    return destinationId;
  }

  public void setDestinationId(Integer destinationId) {
    this.destinationId = destinationId;
  }

  public ItemDefinition.Condition getHidden() {
    return hiddenCondition != null ? ItemDefinition.Condition.valueOf(hiddenCondition) : null;
  }

  public void setHidden(ItemDefinition.Condition hidden) {
    this.hiddenCondition = hidden != null ? hidden.name() : null;
  }

  public ItemDefinition.Condition getDisabled() {
    return disabledCondition != null ? ItemDefinition.Condition.valueOf(disabledCondition) : null;
  }

  public void setDisabled(ItemDefinition.Condition disabled) {
    this.disabledCondition = disabled != null ? disabled.name() : null;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public Boolean getGlobal() {
    return global;
  }

  public void setGlobal(Boolean global) {
    this.global = global;
  }

  public List<ItemTextsEntity> getTexts() {
    return texts;
  }

  public void setTexts(List<ItemTextsEntity> texts) {
    this.texts = texts != null ? texts : new ArrayList<>();
  }

  public Set<FormTypeRef> getFormTypes() {
    return formTypes;
  }

  public void setFormTypes(Set<FormTypeRef> formTypes) {
    this.formTypes = formTypes != null ? formTypes : new HashSet<>();
  }

  public List<PrefillStrategyRef> getPrefillStrategyRefs() {
    return prefillStrategyRefs;
  }

  public void setPrefillStrategyRefs(List<PrefillStrategyRef> prefillStrategyRefs) {
    this.prefillStrategyRefs = prefillStrategyRefs != null ? prefillStrategyRefs : new ArrayList<>();
  }
}
