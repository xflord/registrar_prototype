package org.perun.registrarprototype.models;

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
    return data != null && data.matches(regex);
  }
}
