package org.perun.registrarprototype.core.services.config;

import org.perun.registrarprototype.core.security.CurrentUser;
import org.perun.registrarprototype.core.services.AuthorizationService;

public class AuthorizationServiceDummy implements AuthorizationService {
  @Override
  public boolean isAuthorized(CurrentUser sess, int groupId) {
    return true;
  }
}
