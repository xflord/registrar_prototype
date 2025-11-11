package org.perun.registrarprototype.controllers.dto;

import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.FormSpecification;

public class ApplicationDTO {
  private int id;
  private Integer formSpecificationId;
  private ApplicationState state;
  private String submitterName;
  private Integer submissionId;
  private FormSpecification.FormType type;

  public ApplicationDTO() {}

  public ApplicationDTO(int id, Integer formSpecificationId, ApplicationState state, String submitterName, Integer submissionId, FormSpecification.FormType type) {
    this.id = id;
    this.formSpecificationId = formSpecificationId;
    this.state = state;
    this.submitterName = submitterName;
    this.submissionId = submissionId;
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  public ApplicationState getState() {
    return state;
  }

  public void setState(ApplicationState state) {
    this.state = state;
  }

  public String getSubmitterName() {
    return submitterName;
  }

  public void setSubmitterName(String submitterName) {
    this.submitterName = submitterName;
  }

  public Integer getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Integer submissionId) {
    this.submissionId = submissionId;
  }

  public FormSpecification.FormType getType() {
    return type;
  }

  public void setType(FormSpecification.FormType type) {
    this.type = type;
  }
}


