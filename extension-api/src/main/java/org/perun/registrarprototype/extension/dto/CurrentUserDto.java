package org.perun.registrarprototype.extension.dto;

import java.util.Map;
import java.util.Set;

public class CurrentUserDto {
  private String id;
  private Set<Integer> managedGroups;
  private Map<String, Object> attributes;
  private boolean authenticated;

  public CurrentUserDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<Integer> getManagedGroups() {
    return managedGroups;
  }

  public void setManagedGroups(Set<Integer> managedGroups) {
    this.managedGroups = managedGroups;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
  }
}
