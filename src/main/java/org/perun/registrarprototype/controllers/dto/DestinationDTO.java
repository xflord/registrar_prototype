package org.perun.registrarprototype.controllers.dto;

public class DestinationDTO {
  private String urn;
  private Integer formSpecificationId;
  private boolean global;

  public DestinationDTO() {}

  public DestinationDTO(String urn, Integer formSpecificationId, boolean global) {
    this.urn = urn;
    this.formSpecificationId = formSpecificationId;
    this.global = global;
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  public boolean isGlobal() {
    return global;
  }

  public void setGlobal(boolean global) {
    this.global = global;
  }
}
