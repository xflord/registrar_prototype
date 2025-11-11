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

  public SubmissionDTO() {}

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
  public void setId(int id) { this.id = id; }
  
  public LocalDateTime getTimestamp() { return timestamp; }
  public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
  
  public Integer getSubmitterId() { return submitterId; }
  public void setSubmitterId(Integer submitterId) { this.submitterId = submitterId; }
  
  public String getSubmitterName() { return submitterName; }
  public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }
  
  public String getIdentityIdentifier() { return identityIdentifier; }
  public void setIdentityIdentifier(String identityIdentifier) { this.identityIdentifier = identityIdentifier; }
  
  public String getIdentityIssuer() { return identityIssuer; }
  public void setIdentityIssuer(String identityIssuer) { this.identityIssuer = identityIssuer; }
  
  public Map<String, String> getIdentityAttributes() { return identityAttributes; }
  public void setIdentityAttributes(Map<String, String> identityAttributes) { this.identityAttributes = identityAttributes; }
}

