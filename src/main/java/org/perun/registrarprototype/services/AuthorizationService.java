package org.perun.registrarprototype.services;

import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;

public interface AuthorizationService {
  // in the future make more granular via policies or multiple methods/operation argument
  boolean isAuthorized(RegistrarAuthenticationToken sess, int groupId);

}
