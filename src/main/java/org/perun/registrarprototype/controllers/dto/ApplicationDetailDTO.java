package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItemData;

public class ApplicationDetailDTO extends ApplicationDTO {
  private List<FormItemData> formItemData;
  private SubmissionDTO submission;
  private DecisionDTO latestDecision;

  public ApplicationDetailDTO(int id, Form form, ApplicationState state, String submitterName, Integer submissionId,
                              Form.FormType type, List<FormItemData> formItemData, SubmissionDTO submission,
                              DecisionDTO latestDecision) {
    super(id, form, state, submitterName, submissionId, type);
    this.formItemData = formItemData;
    this.submission = submission;
    this.latestDecision = latestDecision;
  }

  public List<FormItemData> getFormItemData() { return formItemData; }
  public SubmissionDTO getSubmission() { return submission; }
  public DecisionDTO getLatestDecision() { return latestDecision; }
}


