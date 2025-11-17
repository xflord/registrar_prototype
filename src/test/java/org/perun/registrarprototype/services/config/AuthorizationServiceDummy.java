package org.perun.registrarprototype.services.config;

import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.services.AuthorizationService;

public class AuthorizationServiceDummy implements AuthorizationService {
  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, String groupId) {
    System.out.println("AuthorizationServiceDummy.canManage");
    return true;
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, Application app) {
    return false;
  }

  @Override
  public boolean canDecide(RegistrarAuthenticationToken sess, String groupId) {
    return true;
  }

  @Override
  public boolean isAdmin(RegistrarAuthenticationToken sess) {
    return false;
  }
}
