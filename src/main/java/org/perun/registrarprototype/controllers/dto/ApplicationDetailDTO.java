package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.FormSpecification;

public class ApplicationDetailDTO extends ApplicationDTO {
  private List<FormItemDataDTO> formItemData;
  private SubmissionDTO submission;
  private DecisionDTO latestDecision;

  public ApplicationDetailDTO() {
    super();
  }

  public ApplicationDetailDTO(int id, Integer formSpecificationId, ApplicationState state, String submitterName, Integer submissionId,
                              FormSpecification.FormType type, List<FormItemDataDTO> formItemData, SubmissionDTO submission,
                              DecisionDTO latestDecision) {
    super(id, formSpecificationId, state, submitterName, submissionId, type);
    this.formItemData = formItemData;
    this.submission = submission;
    this.latestDecision = latestDecision;
  }

  public List<FormItemDataDTO> getFormItemData() { 
    return formItemData; 
  }

  public void setFormItemData(List<FormItemDataDTO> formItemData) {
    this.formItemData = formItemData;
  }

  public SubmissionDTO getSubmission() { 
    return submission; 
  }

  public void setSubmission(SubmissionDTO submission) {
    this.submission = submission;
  }

  public DecisionDTO getLatestDecision() { 
    return latestDecision; 
  }

  public void setLatestDecision(DecisionDTO latestDecision) {
    this.latestDecision = latestDecision;
  }
}


