package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import org.perun.registrarprototype.models.FormSpecification;

public class ApplicationFormDTO {
  private Integer formSpecificationId;
  private List<FormItemDataDTO> formItemData;
  private FormSpecification.FormType type;

  public ApplicationFormDTO() {}

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  public List<FormItemDataDTO> getFormItemData() {
    return formItemData;
  }

  public void setFormItemData(List<FormItemDataDTO> formItemData) {
    this.formItemData = formItemData;
  }

  public FormSpecification.FormType getType() {
    return type;
  }

  public void setType(FormSpecification.FormType type) {
    this.type = type;
  }
}

