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

  public int getItemId() {
    return itemId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
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
    return "FormItemData [itemId=" + itemId + ", data=" + value + "]";
  }
}
