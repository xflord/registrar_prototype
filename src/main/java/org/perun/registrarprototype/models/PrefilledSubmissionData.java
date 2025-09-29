package org.perun.registrarprototype.models;

import java.util.List;

public class PrefilledSubmissionData {
  private String redirectUrl;
  private List<PrefilledFormData> prefilledData; // TODO maybe make this a map (but then how de we know the key is a VO/Group?)
  
  public PrefilledSubmissionData(String redirectUrl, List<PrefilledFormData> prefilledData) {
    this.redirectUrl = redirectUrl;
    this.prefilledData = prefilledData;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public List<PrefilledFormData> getPrefilledData() {
    return prefilledData;
  }
}

