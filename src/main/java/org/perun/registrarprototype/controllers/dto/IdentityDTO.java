package org.perun.registrarprototype.controllers.dto;

import java.util.Map;

public class IdentityDTO {
  private String name;
  private String organization;
  private String email;
  private String type;
  private Map<String, String> attributes;

  public IdentityDTO() {
  }

  public IdentityDTO(String name, String organization, String email, String type, Map<String, String> attributes) {
    this.name = name;
    this.organization = organization;
    this.email = email;
    this.type = type;
    this.attributes = attributes;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }
}

