package org.perun.registrarprototype.controllers.dto;

import org.perun.registrarprototype.models.ApplicationState;

public class ApplicationDTO {
  private int id;
  private int formId;
  private ApplicationState state;
  private String submitterName;
  private Integer submissionId;

  public ApplicationDTO(int id, int formId, ApplicationState state, String submitterName, Integer submissionId) {
    this.id = id;
    this.formId = formId;
    this.state = state;
    this.submitterName = submitterName;
    this.submissionId = submissionId;
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
}


