package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.FormItemData;

public class ApplicationDetailDTO {
  private int id;
  private int formId;
  private ApplicationState state;
  private String submitterName;
  private Integer submissionId;
  private List<FormItemData> formItemData;

  public ApplicationDetailDTO(int id, int formId, ApplicationState state, String submitterName, Integer submissionId,
                              List<FormItemData> formItemData) {
    this.id = id;
    this.formId = formId;
    this.state = state;
    this.submitterName = submitterName;
    this.submissionId = submissionId;
    this.formItemData = formItemData;
  }

  public int getId() { return id; }
  public int getFormId() { return formId; }
  public ApplicationState getState() { return state; }
  public String getSubmitterName() { return submitterName; }
  public Integer getSubmissionId() { return submissionId; }
  public List<FormItemData> getFormItemData() { return formItemData; }
}


