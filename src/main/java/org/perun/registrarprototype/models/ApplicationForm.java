package org.perun.registrarprototype.models;

import java.util.List;

public class ApplicationForm {
  private FormSpecification formSpecification;
  private List<FormItemData> prefilledItems;
  private FormSpecification.FormType type;

  public ApplicationForm(FormSpecification formSpecification, List<FormItemData> prefilledItems, FormSpecification.FormType type) {
    this.formSpecification = formSpecification;
    this.prefilledItems = prefilledItems;
    this.type = type;
  }

  public FormSpecification getForm() {
    return formSpecification;
  }

  public List<FormItemData> getPrefilledItems() {
    return prefilledItems;
  }

  public void setPrefilledItems(List<FormItemData> prefilledItems) {
    this.prefilledItems = prefilledItems;
  }

  public FormSpecification.FormType getType() {
    return type;
  }
}
