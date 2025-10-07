package org.perun.registrarprototype.core.models;

import java.util.List;
import org.perun.registrarprototype.core.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.core.exceptions.InvalidApplicationStateTransitionException;
import org.perun.registrarprototype.extension.dto.ApplicationState;
import org.perun.registrarprototype.extension.dto.FormType;

public class Application {
  private int id;
  private final int userId;
  private final int formId;
  private final List<FormItemData> formItemData;
  private ApplicationState state = ApplicationState.PENDING;
  private String redirectUrl;
  private FormType type;
  private Submission submission;

  public Application(int id, int userId, int formId, List<FormItemData> formItemData, String redirectUrl, FormType type) {
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
  public void setId(int id) {
    this.id = id;
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

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void submit(Form form) throws InvalidApplicationDataException, InvalidApplicationStateTransitionException {
    if (state != ApplicationState.PENDING) {
      throw new InvalidApplicationStateTransitionException("Only new applications can be submitted", this);
    }
    // TODO this is probably unnecessary, consider removing `submit` `approve` and `reject` methods
    ValidationResult result = form.validateItemData(formItemData);
    if (!result.isValid()) {
        throw new InvalidApplicationDataException("Some of the form items were incorrectly filled in",
            result.errors(), this);
    }
    this.state = ApplicationState.SUBMITTED;
  }

  public void approve() throws InvalidApplicationStateTransitionException {
    if (!this.state.isOpenState()) {
      throw new InvalidApplicationStateTransitionException("Application has already been approved or rejected", this);
    }
    this.state = ApplicationState.APPROVED;
  }

  public void reject(String reason) throws InvalidApplicationStateTransitionException {
    if (!this.state.isOpenState()) {
      throw new InvalidApplicationStateTransitionException("Application has already been approved or rejected", this);
    }
    if (reason == null || reason.isBlank()) {
        throw new IllegalArgumentException("Rejection reason is required");
    }
    this.state = ApplicationState.REJECTED;
  }

  public void requestChanges() throws InvalidApplicationStateTransitionException {
    if (!this.state.isOpenState()) {
      throw new InvalidApplicationStateTransitionException("Application has already been approved or rejected", this);
    }
    this.state = ApplicationState.CHANGES_REQUESTED;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public void setState(ApplicationState state) {
    this.state = state;
  }

  public FormType getType() {
    return type;
  }

  public void setType(FormType type) {
    this.type = type;
  }

  public Submission getSubmission() {
    return submission;
  }

  public void setSubmission(Submission submission) {
    this.submission = submission;
  }
}

