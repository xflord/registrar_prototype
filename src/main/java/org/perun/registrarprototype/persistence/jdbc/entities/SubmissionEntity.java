package org.perun.registrarprototype.persistence.jdbc.entities;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("submission")
public class SubmissionEntity extends AuditEntity {
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
  private Map<String, String> identityAttributes;

  public SubmissionEntity() {
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

  public Map<String, String> getIdentityAttributes() {
    return identityAttributes;
  }

  public void setIdentityAttributes(
      Map<String, String> identityAttributes) {
    this.identityAttributes = identityAttributes;
  }
}