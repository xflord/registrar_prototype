package org.perun.registrarprototype.controllers.dto;

import java.util.List;

public class SubmissionContextDTO {
  private String redirectUrl;
  private List<ApplicationFormDTO> prefilledData;

  public SubmissionContextDTO() {}

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public List<ApplicationFormDTO> getPrefilledData() {
    return prefilledData;
  }

  public void setPrefilledData(List<ApplicationFormDTO> prefilledData) {
    this.prefilledData = prefilledData;
  }
}

