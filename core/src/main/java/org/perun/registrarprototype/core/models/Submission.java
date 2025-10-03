package org.perun.registrarprototype.core.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Aggregates all applications submitted at a given time by a user. Contains identity attributes of the principal,
 * ideally allowing us to link submitted applications to a user (along with similarUsers). That should allow us to modify the
 * identity info associated with the applications at once (e.g., when a user modifies one app, all apps matched by submission
 * data will be updated).
 */
public class Submission {
  private List<Application> applications;
  private LocalDateTime timestamp;
  private String submitterId;
  private String submitterName;
  private Map<String, String> identityAttributes; // more or less the equivalent of `fedInfo`

  public Submission() {
  }

  public Submission(List<Application> applications, LocalDateTime timestamp, String submitterId, String submitterName, Map<String, String> identityAttributes) {
    this.applications = applications;
    this.timestamp = timestamp;
    this.submitterId = submitterId;
    this.submitterName = submitterName;
    this.identityAttributes = identityAttributes;
  }

  public List<Application> getApplications() {
    return applications;
  }

  public void setApplications(List<Application> applications) {
    this.applications = applications;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getSubmitterId() {
    return submitterId;
  }

  public void setSubmitterId(String submitterId) {
    this.submitterId = submitterId;
  }

  public String getSubmitterName() {
    return submitterName;
  }

  public void setSubmitterName(String submitterName) {
    this.submitterName = submitterName;
  }

  public Map<String, String> getIdentityAttributes() {
    return identityAttributes;
  }

  public void setIdentityAttributes(Map<String, String> identityAttributes) {
    this.identityAttributes = identityAttributes;
  }
}
