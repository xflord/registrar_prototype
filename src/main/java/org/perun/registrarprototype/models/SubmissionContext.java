package org.perun.registrarprototype.models;

import java.util.List;

public class SubmissionContext {
  private String redirectUrl;
  private List<ApplicationContext> prefilledData; // TODO maybe make this a map (but then how de we know the key is a VO/Group?)
  
  public SubmissionContext(String redirectUrl, List<ApplicationContext> prefilledData) {
    this.redirectUrl = redirectUrl;
    this.prefilledData = prefilledData;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public List<ApplicationContext> getPrefilledData() {
    return prefilledData;
  }
}

