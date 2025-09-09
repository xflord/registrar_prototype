package org.perun.registrarprototype.models;

import java.util.regex.Pattern;

public class FormItemData {
  private final int itemId;
  private String data;

  public FormItemData(int itemId, String data) {
    this.itemId = itemId;
    this.data = data;
  }

  public int getItemId() {
    return itemId;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public boolean isEmpty() {
    return data == null || data.isBlank();
  }

  public boolean matches(String regex) {
    Pattern pattern = Pattern.compile(regex);
    return data != null && pattern.matcher(data).matches();
  }

  @Override
  public String toString() {
    return "FormItemData [itemId=" + itemId + ", data=" + data + "]";
  }
}
