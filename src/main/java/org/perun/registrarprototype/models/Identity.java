package org.perun.registrarprototype.models;

import java.util.Map;

/**
 * Similar identities from different providers, display and GUI and offer option redirect to consolidator
 */
public class Identity {

  private String name;
  private String organization;
  private String email;
  private String type;
  private Map<String, String> attributes; // TODO which attributes are important to display? (probably `sourceIdPName`, but how is it different from ExtSource name?

  public Identity(String name, String organization, String email, String type, Map<String, String> attributes) {
    this.name = name;
    this.organization = organization;
    this.email = email;
    this.type = type;
    this.attributes = attributes;
  }

  public String getName() {
    return name;
  }

  public String getOrganization() {
    return organization;
  }

  public String getEmail() {
    return email;
  }

  public String getType() {
    return type;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }
}
