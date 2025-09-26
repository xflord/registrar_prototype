package org.perun.registrarprototype.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;

public class Application {
  private final int id;
  private final int userId;
  private final int formId;
  private final List<FormItemData> formItemData;
  private ApplicationState state = ApplicationState.PENDING;
  private String rejectionReason;
  private Map<String, String> externalAttributes = new HashMap<>();
  private String redirectUrl;
  private Form.FormType type;

  public Application(int id, int userId, int formId, List<FormItemData> formItemData, String redirectUrl, Form.FormType type) {
    this.id = id;
    this.userId = userId;
    this.formId = formId;
    this.formItemData = formItemData;
    this.redirectUrl = redirectUrl;
    this.type = type;
  }

  public int getId() {
    return id;
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

  public Map<String, String> getExternalAttributes() {
    return externalAttributes;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void submit(Form form) throws InvalidApplicationDataException {
    ValidationResult result = form.validateItemData(formItemData);
    if (!result.isValid()) {
        throw new InvalidApplicationDataException("Some of the form items were incorrectly filled in",
            result.errors(), this);
    }
    this.state = ApplicationState.PENDING;
  }

  public void approve() throws InvalidApplicationStateTransitionException {
    if (state != ApplicationState.PENDING) {
      throw new InvalidApplicationStateTransitionException("Application has already been approved or rejected", this);
    }
    this.state = ApplicationState.APPROVED;
  }

  public void reject(String reason) throws InvalidApplicationStateTransitionException {
    if (state != ApplicationState.PENDING) {
      throw new InvalidApplicationStateTransitionException("Application has already been approved or rejected", this);
    }
    if (reason == null || reason.isBlank()) {
        throw new IllegalArgumentException("Rejection reason is required");
    }
    this.state = ApplicationState.REJECTED;
    this.rejectionReason = reason;
  }

  public void setExternalAttributes(Map<String, String> externalAttributes) {
    this.externalAttributes = externalAttributes;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }
}

