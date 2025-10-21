package org.perun.registrarprototype.models;

import java.util.List;
import java.util.Objects;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;

public class Application {
  private int id;
  private Integer idmUserId;
  private FormSpecification formSpecification;
  private final List<FormItemData> formItemData;
  private ApplicationState state = ApplicationState.PENDING;
  private String redirectUrl;
  private FormSpecification.FormType type;
  private Submission submission = new Submission();

  public Application(int id, Integer idmUserId, FormSpecification formSpecification, List<FormItemData> formItemData, String redirectUrl, FormSpecification.FormType type) {
    this.id = id;
    this.idmUserId = idmUserId;
    this.formSpecification = formSpecification;
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

  public Integer getIdmUserId() {
    return idmUserId;
  }
  public void setIdmUserId(Integer idmUserId) {
    this.idmUserId = idmUserId;
  }

  public List<FormItemData> getFormItemData() {
    return List.copyOf(formItemData);
  }

  public ApplicationState getState() {
    return state;
  }

  public FormSpecification getForm() {
    return formSpecification;
  }

  public void setForm(FormSpecification formSpecification) {
    this.formSpecification = formSpecification;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void submit(FormSpecification formSpecification) throws InvalidApplicationDataException, InvalidApplicationStateTransitionException {
    if (state != ApplicationState.PENDING) {
      throw new InvalidApplicationStateTransitionException("Only new applications can be submitted", this);
    }
    // TODO this is probably unnecessary, consider removing `submit` `approve` and `reject` methods
    ValidationResult result = formSpecification.validateItemData(formItemData);
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

  public Submission getSubmission() {
    return submission;
  }

  public void setSubmission(Submission submission) {
    this.submission = submission;
  }

  public FormSpecification.FormType getType() {
    return type;
  }

  public void setType(FormSpecification.FormType type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Application that = (Application) o;
    return getId() == that.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}

