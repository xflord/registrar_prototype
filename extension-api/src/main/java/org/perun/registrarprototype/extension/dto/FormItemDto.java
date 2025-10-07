package org.perun.registrarprototype.extension.dto;

import java.util.List;

public class FormItemDto {
  private int id;
  private int formId;
  private ItemType type;
  //texts
  private boolean required;
  private String constraint; // regex or similar
  private String sourceIdentityAttribute;
  private String sourceIdmAttribute;
  private String destinationIdmAttribute;
  private boolean preferIdentityAttribute; // use IdM value if false, oauth claim value if true (and available)
  private String defaultValue;
  private List<FormType> formTypes = List.of(FormType.INITIAL, FormType.EXTENSION);
  private ItemCondition hidden;
  private ItemCondition disabled;
  private Integer hiddenDependencyItemId;
  private Integer disabledDependencyItemId;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getFormId() {
    return formId;
  }

  public void setFormId(int formId) {
    this.formId = formId;
  }

  public ItemType getType() {
    return type;
  }

  public void setType(ItemType type) {
    this.type = type;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getConstraint() {
    return constraint;
  }

  public void setConstraint(String constraint) {
    this.constraint = constraint;
  }

  public String getSourceIdentityAttribute() {
    return sourceIdentityAttribute;
  }

  public void setSourceIdentityAttribute(String sourceIdentityAttribute) {
    this.sourceIdentityAttribute = sourceIdentityAttribute;
  }

  public String getSourceIdmAttribute() {
    return sourceIdmAttribute;
  }

  public void setSourceIdmAttribute(String sourceIdmAttribute) {
    this.sourceIdmAttribute = sourceIdmAttribute;
  }

  public String getDestinationIdmAttribute() {
    return destinationIdmAttribute;
  }

  public void setDestinationIdmAttribute(String destinationIdmAttribute) {
    this.destinationIdmAttribute = destinationIdmAttribute;
  }

  public boolean isPreferIdentityAttribute() {
    return preferIdentityAttribute;
  }

  public void setPreferIdentityAttribute(boolean preferIdentityAttribute) {
    this.preferIdentityAttribute = preferIdentityAttribute;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public List<FormType> getFormTypes() {
    return formTypes;
  }

  public void setFormTypes(List<FormType> formTypes) {
    this.formTypes = formTypes;
  }

  public ItemCondition getHidden() {
    return hidden;
  }

  public void setHidden(ItemCondition hidden) {
    this.hidden = hidden;
  }

  public ItemCondition getDisabled() {
    return disabled;
  }

  public void setDisabled(ItemCondition disabled) {
    this.disabled = disabled;
  }

  public Integer getHiddenDependencyItemId() {
    return hiddenDependencyItemId;
  }

  public void setHiddenDependencyItemId(Integer hiddenDependencyItemId) {
    this.hiddenDependencyItemId = hiddenDependencyItemId;
  }

  public Integer getDisabledDependencyItemId() {
    return disabledDependencyItemId;
  }

  public void setDisabledDependencyItemId(Integer disabledDependencyItemId) {
    this.disabledDependencyItemId = disabledDependencyItemId;
  }
}
