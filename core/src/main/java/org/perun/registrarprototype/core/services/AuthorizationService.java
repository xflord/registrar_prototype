package org.perun.registrarprototype.core.services;

import org.perun.registrarprototype.core.security.CurrentUser;

public interface AuthorizationService {
  // in the future make more granular via policies or multiple methods/operation argument
  boolean isAuthorized(CurrentUser sess, int groupId);

}
