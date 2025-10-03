package org.perun.registrarprototype.core.security;

import java.util.Map;
import java.util.Set;

/**
 * Temporary class to encapsulate user data.
 * This will likely be replaced by an extension of the Spring security principal class
 */
public class CurrentUser {
  private final int id;
  private final Set<Integer> managedGroups;
  private UserInfoEnrichedPrincipal principal = null;

  public CurrentUser(int id, Set<Integer> managedGroups) {
    this.id = id;
    this.managedGroups = managedGroups;
  }

  public CurrentUser(int id, Set<Integer> managedGroups, UserInfoEnrichedPrincipal principal) {
    this.id = id;
    this.managedGroups = managedGroups;
    this.principal = principal;
  }

  public String id() {
    // TODO figure this out -> hold internal ID or nah, etc.
    return principal == null ? String.valueOf(id) : principal.getAttributes().get("sub").toString();
  }
  public Set<Integer> managedGroups() { return managedGroups; }
  public String attribute(String name) { return principal.getClaimAsString(name); }
  public boolean isAuthenticated() { return principal != null; }
  public Map<String, Object> getAttributes() { return principal.getAttributes(); }
}
