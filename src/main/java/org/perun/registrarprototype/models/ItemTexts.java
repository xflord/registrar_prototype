package org.perun.registrarprototype.models;

public class ItemTexts {
  private final String label;
  private final String hint;
  private final String error;

  public ItemTexts(String label, String hint, String error) {
    this.label = label;
    this.hint = hint;
    this.error = error;
  }

  public String getLabel() { return label; }
  public String getHint() { return hint; }
  public String getError() { return error; }
}
