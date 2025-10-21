package org.perun.registrarprototype.models;

import java.util.List;

public class SubmissionContext {
  private String redirectUrl;
  private List<ApplicationForm> prefilledData; // TODO maybe make this a map (but then how de we know the key is a VO/Group?)
  
  public SubmissionContext(String redirectUrl, List<ApplicationForm> prefilledData) {
    this.redirectUrl = redirectUrl;
    this.prefilledData = prefilledData;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public List<ApplicationForm> getPrefilledData() {
    return prefilledData;
  }
}

