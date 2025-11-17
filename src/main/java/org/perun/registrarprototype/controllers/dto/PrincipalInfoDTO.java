package org.perun.registrarprototype.controllers.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.Role;

public class PrincipalInfoDTO {
  private boolean authenticated;
  private String userId;
  private String userName;
  private Map<String, Object> attributes;
  private Map<String, Set<Integer>> roles; // String key for JSON serialization

  // Full constructor
  public PrincipalInfoDTO(boolean authenticated, String userId, String userName,
                         Map<String, Object> attributes, Map<Role, Set<Integer>> roles) {
    this.authenticated = authenticated;
    this.userId = userId;
    this.userName = userName;
    this.attributes = attributes;
    // Convert Role enum to String for JSON
    this.roles = roles != null ?
      roles.entrySet().stream().collect(
        Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)
      ) : new HashMap<>();
  }

  // Unauthenticated constructor
  public PrincipalInfoDTO(boolean authenticated) {
    this.authenticated = authenticated;
    this.roles = new HashMap<>();
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public Map<String, Set<Integer>> getRoles() {
    return roles;
  }

  public void setRoles(Map<String, Set<Integer>> roles) {
    this.roles = roles;
  }
}