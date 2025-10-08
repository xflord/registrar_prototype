package org.perun.registrarprototype.services.config;

import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.services.AuthorizationService;

public class AuthorizationServiceDummy implements AuthorizationService {
  @Override
  public boolean isAuthorized(RegistrarAuthenticationToken sess, int groupId) {
    return true;
  }
}
