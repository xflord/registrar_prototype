package org.perun.registrarprototype.services;

import org.perun.registrarprototype.security.CurrentUser;

public class AuthorizationServiceImpl implements AuthorizationService {
  @Override
  public boolean isAuthorized(CurrentUser sess, int groupId) {
    return sess.managedGroups().contains(groupId);
  }
}
