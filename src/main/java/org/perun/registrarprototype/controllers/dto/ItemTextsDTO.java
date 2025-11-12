package org.perun.registrarprototype.controllers.dto;

public class ItemTextsDTO {
  private String label;
  private String help;
  private String error;

  public ItemTextsDTO() {}

  public ItemTextsDTO(String label, String help, String error) {
    this.label = label;
    this.help = help;
    this.error = error;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getHelp() {
    return help;
  }

  public void setHelp(String help) {
    this.help = help;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}

