package org.perun.registrarprototype.services.config;

import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.services.AuthorizationService;

public class AuthorizationServiceDummy implements AuthorizationService {
  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, int groupId) {
    System.out.println("AuthorizationServiceDummy.canManage");
    return true;
  }

  @Override
  public boolean canDecide(RegistrarAuthenticationToken sess, int groupId) {
    return true;
  }
}
