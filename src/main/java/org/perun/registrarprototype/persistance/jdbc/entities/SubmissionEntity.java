package org.perun.registrarprototype.persistance.jdbc.entities;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("submission")
public class SubmissionEntity {
  @Id
  @Column("id")
  private Integer id;
  @Column("timestamp")
  private LocalDateTime timestamp;
  @Column("submitter_id")
  private String submitterId;
  @Column("submitter_name")
  private String submitterName;
  @Column("identity_identifier")
  private String identityIdentifier;
  @Column("identity_issuer")
  private String identityIssuer;
  @Column("identity_attributes")
  private String identityAttributes; // JSON string representation

  public SubmissionEntity() {
  }

  public SubmissionEntity(Integer id, LocalDateTime timestamp, String submitterId, String submitterName,
                          String identityIdentifier, String identityIssuer, String identityAttributes) {
    this.id = id;
    this.timestamp = timestamp;
    this.submitterId = submitterId;
    this.submitterName = submitterName;
    this.identityIdentifier = identityIdentifier;
    this.identityIssuer = identityIssuer;
    this.identityAttributes = identityAttributes;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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

  public String getIdentityIdentifier() {
    return identityIdentifier;
  }

  public void setIdentityIdentifier(String identityIdentifier) {
    this.identityIdentifier = identityIdentifier;
  }

  public String getIdentityIssuer() {
    return identityIssuer;
  }

  public void setIdentityIssuer(String identityIssuer) {
    this.identityIssuer = identityIssuer;
  }

  public String getIdentityAttributes() {
    return identityAttributes;
  }

  public void setIdentityAttributes(String identityAttributes) {
    this.identityAttributes = identityAttributes;
  }
}