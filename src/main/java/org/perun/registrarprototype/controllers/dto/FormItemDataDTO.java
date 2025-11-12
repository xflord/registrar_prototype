package org.perun.registrarprototype.controllers.dto;

public class FormItemDataDTO {
  private Integer formItemId;
  private String value;
  private String prefilledValue;
  private String identityAttributeValue;
  private String idmAttributeValue;
  private Boolean valueAssured;

  public FormItemDataDTO() {}

  public Integer getFormItemId() {
    return formItemId;
  }

  public void setFormItemId(Integer formItemId) {
    this.formItemId = formItemId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getPrefilledValue() {
    return prefilledValue;
  }

  public void setPrefilledValue(String prefilledValue) {
    this.prefilledValue = prefilledValue;
  }

  public String getIdentityAttributeValue() {
    return identityAttributeValue;
  }

  public void setIdentityAttributeValue(String identityAttributeValue) {
    this.identityAttributeValue = identityAttributeValue;
  }

  public String getIdmAttributeValue() {
    return idmAttributeValue;
  }

  public void setIdmAttributeValue(String idmAttributeValue) {
    this.idmAttributeValue = idmAttributeValue;
  }

  public Boolean getValueAssured() {
    return valueAssured;
  }

  public void setValueAssured(Boolean valueAssured) {
    this.valueAssured = valueAssured;
  }
}

