package org.perun.registrarprototype.services;

import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;

public interface AuthorizationService {
  // in the future make more granular via policies or multiple methods/operation argument
  boolean canManage(RegistrarAuthenticationToken sess, int groupId);

  boolean canDecide(RegistrarAuthenticationToken sess, int groupId);

  boolean isAdmin(RegistrarAuthenticationToken sess);
}
