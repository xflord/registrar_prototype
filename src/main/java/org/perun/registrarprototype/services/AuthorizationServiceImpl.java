package org.perun.registrarprototype.services;

import org.perun.registrarprototype.security.CurrentUser;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {
  @Override
  public boolean isAuthorized(CurrentUser sess, int groupId) {
    return sess.managedGroups().contains(groupId);
  }
}
