package org.perun.registrarprototype.persistence.jdbc.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("item_definition")
public class ItemDefinitionEntity extends AuditEntity {

  @Id
  @Column("id")
  private Integer id;

  @Column("form_specification_id")
  private Integer formSpecificationId;

  @Column("display_name")
  private String displayName;

  @Column("type")
  private String type;

  @Column("updatable")
  private Boolean updatable;

  @Column("required")
  private Boolean required;

  @Column("validator")
  private String validator;

  @Column("destination_id")
  private Integer destinationId;

  @Column("hidden")
  private String hidden;

  @Column("disabled")
  private String disabled;

  @Column("default_value")
  private String defaultValue;

  @Column("global")
  private Boolean global;

  @MappedCollection(idColumn = "item_definition_id", keyColumn = "id")
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
    return hidden != null ? ItemDefinition.Condition.valueOf(hidden) : null;
  }

  public void setHidden(ItemDefinition.Condition hidden) {
    this.hidden = hidden != null ? hidden.name() : null;
  }

  public ItemDefinition.Condition getDisabled() {
    return disabled != null ? ItemDefinition.Condition.valueOf(disabled) : null;
  }

  public void setDisabled(ItemDefinition.Condition disabled) {
    this.disabled = disabled != null ? disabled.name() : null;
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

  public Set<FormSpecification.FormType> getFormTypes() {
    return formTypes.stream().map(FormTypeRef::getFormType).collect(Collectors.toSet());
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
