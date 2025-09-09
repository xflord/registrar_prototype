package org.perun.registrarprototype.services;

import org.perun.registrarprototype.security.CurrentUser;

public interface AuthorizationService {
  // in the future make more granular via policies or multiple methods/operation argument
  boolean isAuthorized(CurrentUser sess, int groupId);

}
