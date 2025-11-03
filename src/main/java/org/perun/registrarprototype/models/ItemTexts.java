package org.perun.registrarprototype.models;

public class ItemTexts {
  private final String label;
  private final String help;
  private final String error;

  public ItemTexts(String label, String help, String error) {
    this.label = label;
    this.help = help;
    this.error = error;
  }

  public String getLabel() { return label; }
  public String getHelp() { return help; }
  public String getError() { return error; }
}
