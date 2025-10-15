package org.perun.registrarprototype.controllers.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class SubmissionDTO {
  private int id;
  private LocalDateTime timestamp;
  private Integer submitterId;
  private String submitterName;
  private String identityIdentifier;
  private String identityIssuer;
  private Map<String, String> identityAttributes;

  public SubmissionDTO(int id, LocalDateTime timestamp, Integer submitterId, String submitterName,
                       String identityIdentifier, String identityIssuer, Map<String, String> identityAttributes) {
    this.id = id;
    this.timestamp = timestamp;
    this.submitterId = submitterId;
    this.submitterName = submitterName;
    this.identityIdentifier = identityIdentifier;
    this.identityIssuer = identityIssuer;
    this.identityAttributes = identityAttributes;
  }

  public int getId() { return id; }
  public LocalDateTime getTimestamp() { return timestamp; }
  public Integer getSubmitterId() { return submitterId; }
  public String getSubmitterName() { return submitterName; }
  public String getIdentityIdentifier() { return identityIdentifier; }
  public String getIdentityIssuer() { return identityIssuer; }
  public Map<String, String> getIdentityAttributes() { return identityAttributes; }
}

