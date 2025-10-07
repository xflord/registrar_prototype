package org.perun.registrarprototype.extension.dto;

import java.util.List;

public class ApplicationDto {
  private int id;
  private int userId;
  private int formId;
  private List<FormItemDataDto> formItemData;
  private ApplicationState state = ApplicationState.PENDING;
  private FormType type;
  private String redirectUrl;
  private int submissionId;
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getFormId() {
    return formId;
  }

  public void setFormId(int formId) {
    this.formId = formId;
  }

  public List<FormItemDataDto> getFormItemData() {
    return formItemData;
  }

  public void setFormItemData(List<FormItemDataDto> formItemData) {
    this.formItemData = formItemData;
  }

  public ApplicationState getState() {
    return state;
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

  public int getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(int submissionId) {
    this.submissionId = submissionId;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }
}
