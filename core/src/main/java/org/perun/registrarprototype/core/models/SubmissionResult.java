package org.perun.registrarprototype.core.models;

import java.util.ArrayList;
import java.util.List;

public class SubmissionResult {
  private List<String> customMessages = new ArrayList<>(); // messages defined by admins to be shown after successful submission
  private String redirectUrl;
  private PrefilledSubmissionData redirectForms; // this might not be necessary as long as we pass the data via redirectUrl, though this would save some api calls
  private Submission submission;

  public SubmissionResult() {
  }

  public SubmissionResult(List<String> customMessages, String redirectUrl,
                          PrefilledSubmissionData redirectForms, Submission submission) {
    this.customMessages = customMessages;
    this.redirectUrl = redirectUrl;
    this.redirectForms = redirectForms;
    this.submission = submission;
  }

  public void addMessage(String message) {
    this.customMessages.add(message);
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

  public PrefilledSubmissionData getRedirectForms() {
    return redirectForms;
  }

  public void setRedirectForms(PrefilledSubmissionData redirectForms) {
    this.redirectForms = redirectForms;
  }

  public Submission getSubmission() {
    return submission;
  }

  public void setSubmission(Submission submission) {
    this.submission = submission;
  }
}
