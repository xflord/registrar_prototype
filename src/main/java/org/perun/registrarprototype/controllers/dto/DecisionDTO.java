package org.perun.registrarprototype.controllers.dto;

import java.time.LocalDateTime;
import org.perun.registrarprototype.models.Decision.DecisionType;

public class DecisionDTO {
  private int id;
  private Integer applicationId;
  private Integer approverId;
  private String approverName;
  private String message;
  private LocalDateTime timestamp;
  private DecisionType decisionType;

  public DecisionDTO(int id, Integer applicationId, Integer approverId, String approverName, String message,
                     LocalDateTime timestamp, DecisionType decisionType) {
    this.id = id;
    this.applicationId = applicationId;
    this.approverId = approverId;
    this.approverName = approverName;
    this.message = message;
    this.timestamp = timestamp;
    this.decisionType = decisionType;
  }

  public int getId() { return id; }
  public Integer getApplicationId() { return applicationId; }
  public Integer getApproverId() { return approverId; }
  public String getApproverName() { return approverName; }
  public String getMessage() { return message; }
  public LocalDateTime getTimestamp() { return timestamp; }
  public DecisionType getDecisionType() { return decisionType; }
}

