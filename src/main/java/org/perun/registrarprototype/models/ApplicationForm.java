package org.perun.registrarprototype.models;

import java.util.List;

public class ApplicationForm {
  private Integer formSpecificationId;
  private List<FormItemData> formItemData;
  private FormSpecification.FormType type;

  public ApplicationForm(Integer formSpecificationId, List<FormItemData> prefilledData, FormSpecification.FormType type) {
    this.formSpecificationId = formSpecificationId;
    this.formItemData = prefilledData;
    this.type = type;
  }

  public void setFormItemData(List<FormItemData> formItemData) {
    this.formItemData = formItemData;
  }

  public FormSpecification.FormType getType() {
    return type;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  public List<FormItemData> getFormItemData() {
    return formItemData;
  }

  public void setType(FormSpecification.FormType type) {
    this.type = type;
  }
}
