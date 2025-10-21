package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItemData;

public class ApplicationDetailDTO extends ApplicationDTO {
  private List<FormItemData> formItemData;
  private SubmissionDTO submission;
  private DecisionDTO latestDecision;

  public ApplicationDetailDTO(int id, FormSpecification formSpecification, ApplicationState state, String submitterName, Integer submissionId,
                              FormSpecification.FormType type, List<FormItemData> formItemData, SubmissionDTO submission,
                              DecisionDTO latestDecision) {
    super(id, formSpecification, state, submitterName, submissionId, type);
    this.formItemData = formItemData;
    this.submission = submission;
    this.latestDecision = latestDecision;
  }

  public List<FormItemData> getFormItemData() { return formItemData; }
  public SubmissionDTO getSubmission() { return submission; }
  public DecisionDTO getLatestDecision() { return latestDecision; }
}


