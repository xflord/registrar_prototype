package org.perun.registrarprototype.controllers.dto;

import java.util.List;

public class SubmissionResultDTO {
  private List<String> customMessages;
  private String redirectUrl;
  private SubmissionContextDTO redirectForms;
  private SubmissionDTO submission;
  private List<Integer> applicationIds;

  public SubmissionResultDTO() {}

  public SubmissionResultDTO(List<String> customMessages, String redirectUrl,
                             SubmissionContextDTO redirectForms, SubmissionDTO submission,
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

  public void setCustomMessages(List<String> customMessages) {
    this.customMessages = customMessages;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public SubmissionContextDTO getRedirectForms() {
    return redirectForms;
  }

  public void setRedirectForms(SubmissionContextDTO redirectForms) {
    this.redirectForms = redirectForms;
  }

  public SubmissionDTO getSubmission() {
    return submission;
  }

  public void setSubmission(SubmissionDTO submission) {
    this.submission = submission;
  }

  public List<Integer> getApplicationIds() {
    return applicationIds;
  }

  public void setApplicationIds(List<Integer> applicationIds) {
    this.applicationIds = applicationIds;
  }
}

