package org.perun.registrarprototype.services;

import org.perun.registrarprototype.models.CurrentUser;

public interface AuthorizationService {
  boolean canApprove(int applicationId);

  CurrentUser fetchPrincipal(String auth);
}
