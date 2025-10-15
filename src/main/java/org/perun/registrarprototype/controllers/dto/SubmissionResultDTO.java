package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import org.perun.registrarprototype.models.SubmissionContext;

public class SubmissionResultDTO {
  private List<String> customMessages;
  private String redirectUrl;
  private SubmissionContext redirectForms;
  private SubmissionDTO submission;
  private List<Integer> applicationIds;

  public SubmissionResultDTO(List<String> customMessages, String redirectUrl,
                             SubmissionContext redirectForms, SubmissionDTO submission,
                             List<Integer> applicationIds) {
    this.customMessages = customMessages;
    this.redirectUrl = redirectUrl;
    this.redirectForms = redirectForms;
    this.submission = submission;
    this.applicationIds = applicationIds;
  }

  public List<String> getCustomMessages() {
    return customMessages;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public SubmissionContext getRedirectForms() {
    return redirectForms;
  }

  public SubmissionDTO getSubmission() {
    return submission;
  }

  public List<Integer> getApplicationIds() {
    return applicationIds;
  }
}

