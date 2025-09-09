package org.perun.registrarprototype.security;

import java.util.Set;

// temporary session object
public class CurrentUser {
  private final int id;
  private final Set<Integer> managedGroups;

  public CurrentUser(int id, Set<Integer> managedGroups) {
    this.id = id;
    this.managedGroups = managedGroups;
  }

  public int id() { return id; }
  public Set<Integer> managedGroups() { return managedGroups; }
}
