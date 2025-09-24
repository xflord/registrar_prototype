package org.perun.registrarprototype.models;

import java.util.regex.Pattern;

public class FormItemData {
  private final int itemId;
  private String value;
  private String prefilledValue; // probably set this in GUI and later check that this value matches current IdM/Identity values

  public FormItemData(int itemId, String value) {
    this.itemId = itemId;
    this.value = value;
  }

  public FormItemData(int itemId, String value, String prefilledValue) {
    this.itemId = itemId;
    this.value = value;
    this.prefilledValue = prefilledValue;
  }

  public int getItemId() {
    return itemId;
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

  public boolean isEmpty() {
    return value == null || value.isBlank();
  }

  public boolean matches(String regex) {
    Pattern pattern = Pattern.compile(regex);
    return value != null && pattern.matcher(value).matches();
  }

  @Override
  public String toString() {
    return "FormItemData [itemId=" + itemId + ", data=" + value + ", prefilledValue= " + prefilledValue +"]";
  }
}
