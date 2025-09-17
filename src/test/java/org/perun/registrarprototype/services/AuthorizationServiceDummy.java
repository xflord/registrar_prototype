package org.perun.registrarprototype.services;

import org.perun.registrarprototype.security.CurrentUser;

public class AuthorizationServiceDummy implements AuthorizationService {
  @Override
  public boolean isAuthorized(CurrentUser sess, int groupId) {
    return true;
  }
}
