package org.perun.registrarprototype.models;

import java.util.List;

public class ApplicationForm {
  private FormSpecification formSpecification;
  private List<FormItemData> formItemData;
  private FormSpecification.FormType type;

  public ApplicationForm(FormSpecification formSpecification, List<FormItemData> prefilledData, FormSpecification.FormType type) {
    this.formSpecification = formSpecification;
    this.formItemData = prefilledData;
    this.type = type;
  }

  public FormSpecification getForm() {
    return formSpecification;
  }

  public void setForm(FormSpecification formSpecification) {
    this.formSpecification = formSpecification;
  }

  public void setFormItemData(List<FormItemData> formItemData) {
    this.formItemData = formItemData;
  }

  public FormSpecification.FormType getType() {
    return type;
  }

  public FormSpecification getFormSpecification() {
    return formSpecification;
  }

  public void setFormSpecification(FormSpecification formSpecification) {
    this.formSpecification = formSpecification;
  }

  public List<FormItemData> getFormItemData() {
    return formItemData;
  }

  public void setType(FormSpecification.FormType type) {
    this.type = type;
  }
}
