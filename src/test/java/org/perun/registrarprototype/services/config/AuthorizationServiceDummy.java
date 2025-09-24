package org.perun.registrarprototype.services.config;

import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.services.AuthorizationService;

public class AuthorizationServiceDummy implements AuthorizationService {
  @Override
  public boolean isAuthorized(CurrentUser sess, int groupId) {
    return true;
  }
}
