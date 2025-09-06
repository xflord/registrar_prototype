package org.perun.registrarprototype.models;

import java.util.List;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;

public class Application {
  private int id;
  private final int groupId;
  private final int userId;
  private final int formId;
  private final List<FormItemData> formItemData;
  private ApplicationState state = ApplicationState.PENDING;
  private String rejectionReason;

  public Application(int id, int groupId, int userId, int formId, List<FormItemData> formItemData) {
    this.id = id;
    this.groupId = groupId;
    this.userId = userId;
    this.formId = formId;
    this.formItemData = formItemData;
  }

  public int getId() {
    return id;
  }

  public int getGroupId() {
    return groupId;
  }

  public int getUserId() {
    return userId;
  }

  public List<FormItemData> getFormItemData() {
    return List.copyOf(formItemData);
  }

  public ApplicationState getState() {
    return state;
  }

  public int getFormId() {
    return formId;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void submit(Form form) throws InvalidApplicationDataException {
    ValidationResult result = form.validateItemData(formItemData);
    if (!result.isValid()) {
        throw new InvalidApplicationDataException("Some of the form items were incorrectly filled in",
            result.errors(), this);
    }
    this.state = ApplicationState.PENDING;
  }

  public void approve() {
    if (state != ApplicationState.PENDING) {
      throw new InvalidApplicationStateTransitionException("Application has already been approved or rejected", this);
    }
    this.state = ApplicationState.APPROVED;
  }

  public void reject(String reason) {
    if (state != ApplicationState.PENDING) {
      throw new InvalidApplicationStateTransitionException("Application has already been approved or rejected", this);
    }
    if (reason == null || reason.isBlank()) {
        throw new IllegalArgumentException("Rejection reason is required");
    }
    this.state = ApplicationState.REJECTED;
    this.rejectionReason = reason;
  }

}

