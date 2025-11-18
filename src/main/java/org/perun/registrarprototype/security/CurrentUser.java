package org.perun.registrarprototype.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;

/**
 * Temporary class to encapsulate user data.
 * This will likely be replaced by an extension of the Spring security principal class
 */
public class CurrentUser {
  private final String id; // TODO do we want to have a separate id for registrar users / perun users?
  private final Set<String> groups;
  private Map<String, Object> attributes = new HashMap<>();
  private Map<Role, Set<String>> roles = new HashMap<>();

  public CurrentUser(String id, Set<String> groups, Map<String, Object> attributes) {
    this.id = id;
    this.groups = groups;
    this.attributes = attributes;
  }

  public CurrentUser() {
    this.id = null;
    this.groups = new HashSet<>();
  }

  public Set<String> groups() { return groups; }
  public String attribute(String name) {
    return getAttributes().get(name) != null ? (String) getAttributes().get(name) : null;
  }
  public Map<String, Object> getAttributes() { return attributes; }

  public String id() { return id; }

  public String name() {
    Object name = this.getAttributes().get("name");
    return name == null ? (String) this.getAttributes().get("sub") : (String) name;
  }

  public Map<Role, Set<String>> getRoles() {
    return roles;
  }

  public void setRoles(Map<Role, Set<String>> roles) {
    this.roles = roles;
  }
}
