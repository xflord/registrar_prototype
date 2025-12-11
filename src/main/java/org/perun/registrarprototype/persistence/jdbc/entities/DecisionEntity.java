package org.perun.registrarprototype.persistence.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("decision")
public class DecisionEntity extends AuditEntity {
  @Id
  @Column("id")
  private Integer id;
  @Column("application_id")
  private Integer applicationId;
  @Column("approver_id")
  private String approverId;
  @Column("approver_name")
  private String approverName;
  @Column("message")
  private String message;
  @Column("timestamp")
  private LocalDateTime timestamp;
  @Column("decision_type")
  private String decisionType;

  public DecisionEntity() {
  }

  public DecisionEntity(Integer id, Integer applicationId, String approverId, String approverName, String message, LocalDateTime timestamp, String decisionType) {
    this.id = id;
    this.applicationId = applicationId;
    this.approverId = approverId;
    this.approverName = approverName;
    this.message = message;
    this.timestamp = timestamp;
    this.decisionType = decisionType;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  public String getApproverId() {
    return approverId;
  }

  public void setApproverId(String approverId) {
    this.approverId = approverId;
  }

  public String getApproverName() {
    return approverName;
  }

  public void setApproverName(String approverName) {
    this.approverName = approverName;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getDecisionType() {
    return decisionType;
  }

  public void setDecisionType(String decisionType) {
    this.decisionType = decisionType;
  }
}