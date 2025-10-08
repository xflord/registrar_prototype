package org.perun.registrarprototype.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Temporary class to encapsulate user data.
 * This will likely be replaced by an extension of the Spring security principal class
 */
public class CurrentUser {
  private final int id;
  private final Set<String> managedGroups;
  private Map<String, Object> attributes = new HashMap<>();

  public CurrentUser(int id, Set<String> managedGroups, Map<String, Object> attributes) {
    this.id = id;
    this.managedGroups = managedGroups;
    this.attributes = attributes;
  }

  public CurrentUser() {
    this.id = -1;
    this.managedGroups = new HashSet<>();
  }

  public Set<String> managedGroups() { return managedGroups; }
  public String attribute(String name) { return getAttributes().get(name).toString(); }
  public Map<String, Object> getAttributes() { return attributes; }

  public String id() {
    Object name = this.getAttributes().get("name");
    return name == null ? (String) this.getAttributes().get("sub"): (String) name;
  }
}
