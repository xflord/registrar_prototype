package org.perun.registrarprototype.models;

import java.time.LocalDateTime;

public class Decision {
  private int id;
  private Application application;
  private String approverId;
  private String approverName;
  private String message;
  private LocalDateTime timestamp;
  private DecisionType decisionType;

  public Decision() {}

  public Decision(int id, Application application, String approverId, String approverName, String message, LocalDateTime timestamp,
                  DecisionType decisionType) {
    this.id = id;
    this.application = application;
    this.approverId = approverId;
    this.approverName = approverName;
    this.message = message;
    this.timestamp = timestamp;
    this.decisionType = decisionType;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
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

  public DecisionType getDecisionType() {
    return decisionType;
  }

  public void setDecisionType(DecisionType decisionType) {
    this.decisionType = decisionType;
  }

  public enum DecisionType {
    APPROVED,
    REJECTED,
    CHANGES_REQUESTED
  }
}
