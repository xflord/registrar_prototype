package org.perun.registrarprototype.extension.dto;

public class FormItemDataDto {
  private FormItemDto formItem;
  private String value;
  private String prefilledValue; // preferred prefilled value passed to GUI
  private String identityAttributeValue;
  private String idmAttributeValue;
  private boolean valueAssured;

  public boolean isValueAssured() {
    return valueAssured;
  }

  public void setValueAssured(boolean valueAssured) {
    this.valueAssured = valueAssured;
  }

  public String getIdmAttributeValue() {
    return idmAttributeValue;
  }

  public void setIdmAttributeValue(String idmAttributeValue) {
    this.idmAttributeValue = idmAttributeValue;
  }

  public String getIdentityAttributeValue() {
    return identityAttributeValue;
  }

  public void setIdentityAttributeValue(String identityAttributeValue) {
    this.identityAttributeValue = identityAttributeValue;
  }

  public String getPrefilledValue() {
    return prefilledValue;
  }

  public void setPrefilledValue(String prefilledValue) {
    this.prefilledValue = prefilledValue;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public FormItemDto getFormItem() {
    return formItem;
  }

  public void setFormItem(FormItemDto formItem) {
    this.formItem = formItem;
  }
}
