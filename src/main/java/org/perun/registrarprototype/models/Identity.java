package org.perun.registrarprototype.models;

/**
 * Similar identities from different providers, display and GUI and offer option redirect to consolidator
 */
public class Identity {

  private String name;
  private String organization;
  private String email;
  private String type;

  public Identity(String name, String organization, String email, String type) {
    this.name = name;
    this.organization = organization;
    this.email = email;
    this.type = type;
  }
}
