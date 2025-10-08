package org.perun.registrarprototype.services;

import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {
  @Override
  public boolean isAuthorized(RegistrarAuthenticationToken sess, int groupId) {
    return sess.getPrincipal().managedGroups().contains(groupId);
  }
}
