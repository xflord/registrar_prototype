package org.perun.registrarprototype.models;

import java.util.List;
import java.util.Objects;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;

public class Application extends ApplicationForm {
  private int id;
  private String idmUserId;
  private ApplicationState state = ApplicationState.PENDING;
  private String redirectUrl;
  private Integer submissionId;

  public Application(int id, String idmUserId, Integer formSpecificationId, List<FormItemData> formItemData, String redirectUrl, FormSpecification.FormType type, ApplicationState state,
                     Integer submissionId) {
    super(formSpecificationId, formItemData, type);
    this.id = id;
    this.idmUserId = idmUserId;
    this.redirectUrl = redirectUrl;
    this.state = state;
    this.submissionId = submissionId;
  }

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }

  public String getIdmUserId() {
    return idmUserId;
  }
  public void setIdmUserId(String idmUserId) {
    this.idmUserId = idmUserId;
  }


  public ApplicationState getState() {
    return state;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void submit(FormSpecification formSpecification) throws InvalidApplicationDataException, InvalidApplicationStateTransitionException {
    if (state != ApplicationState.PENDING) {
      throw new InvalidApplicationStateTransitionException("Only new applications can be submitted", this);
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

  public Integer getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Integer submissionId) {
    this.submissionId = submissionId;
  }

  public void setState(ApplicationState state) {
    this.state = state;
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

  @Override
  public String toString() {
    return "Application{" +
               "id=" + id +
               ", idmUserId='" + idmUserId + '\'' +
               ", state=" + state +
               ", redirectUrl='" + redirectUrl + '\'' +
               ", submissionId=" + submissionId +
               '}';
  }
}

