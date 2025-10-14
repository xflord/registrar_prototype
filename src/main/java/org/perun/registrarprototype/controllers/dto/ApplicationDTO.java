package org.perun.registrarprototype.controllers.dto;

import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.Form;

public class ApplicationDTO {
  private int id;
  private int formId;
  private ApplicationState state;
  private String submitterName;
  private Integer submissionId;
  private Form.FormType type;

  public ApplicationDTO(int id, int formId, ApplicationState state, String submitterName, Integer submissionId, Form.FormType type) {
    this.id = id;
    this.formId = formId;
    this.state = state;
    this.submitterName = submitterName;
    this.submissionId = submissionId;
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public int getFormId() {
    return formId;
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

  public Form.FormType getType() {
    return type;
  }
}


