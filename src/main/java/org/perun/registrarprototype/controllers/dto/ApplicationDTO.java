package org.perun.registrarprototype.controllers.dto;

import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.FormSpecification;

public class ApplicationDTO {
  private int id;
  private FormSpecification formSpecification;
  private ApplicationState state;
  private String submitterName;
  private Integer submissionId;
  private FormSpecification.FormType type;

  public ApplicationDTO(int id, FormSpecification formSpecification, ApplicationState state, String submitterName, Integer submissionId, FormSpecification.FormType type) {
    this.id = id;
    this.formSpecification = formSpecification;
    this.state = state;
    this.submitterName = submitterName;
    this.submissionId = submissionId;
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public FormSpecification getForm() {
    return formSpecification;
  }

  public ApplicationState getState() {
    return state;
  }

  public String getSubmitterName() {
    return submitterName;
  }

  public Integer getSubmissionId() {
    return submissionId;
  }

  public FormSpecification.FormType getType() {
    return type;
  }
}


