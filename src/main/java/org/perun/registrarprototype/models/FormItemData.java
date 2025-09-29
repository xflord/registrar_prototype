package org.perun.registrarprototype.models;

import java.util.regex.Pattern;

public class FormItemData {
  private FormItem formItem;
  private String value;
  private String prefilledValue; // preferred prefilled value passed to GUI
  private String identityAttributeValue;
  private String idmAttributeValue;
  private boolean valueAssured;

  public FormItemData(FormItem formItem, String value) {
    this.formItem = formItem;
    this.value = value;
  }

  public FormItemData(FormItem formItem, String value, String prefilledValue) {
    this.formItem = formItem;
    this.value = value;
    this.prefilledValue = prefilledValue;
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

  public FormItem getFormItem() {
    return formItem;
  }

  public void setFormItem(FormItem formItem) {
    this.formItem = formItem;
  }

  public boolean isValueAssured() {
    return valueAssured;
  }

  public void setValueAssured(boolean valueAssured) {
    this.valueAssured = valueAssured;
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

  public boolean isEmpty() {
    return value == null || value.isBlank();
  }

  public boolean matches(String regex) {
    Pattern pattern = Pattern.compile(regex);
    return value != null && pattern.matcher(value).matches();
  }

  @Override
  public String toString() {
    return "FormItemData [itemId=" + formItem.getFormId() + ", data=" + value + ", prefilledValue= " + prefilledValue +"]";
  }
}
