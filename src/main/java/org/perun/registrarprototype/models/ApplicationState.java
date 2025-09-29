package org.perun.registrarprototype.models;

import java.util.Set;

public enum ApplicationState {
  PENDING,
  SUBMITTED,
  APPROVED,
  REJECTED,
  CHANGES_REQUESTED; // e.g. when some form is not up to standard, admin can request changes

  public static final Set<ApplicationState> OPEN_STATES = Set.of(SUBMITTED, CHANGES_REQUESTED);

  public boolean isOpenState() {
    return OPEN_STATES.contains(this);
  }
}
