package org.perun.registrarprototype.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.stereotype.Service;

// Role-based access control implementation
// TODO discuss how exactly to implement group-based access control, (concurrency with perun etc.)
@Service
public class AuthorizationServiceImpl implements AuthorizationService {

  private final IdMService idmService;
  private final RoleService roleService;

  public AuthorizationServiceImpl(IdMService idmService, RoleService roleService) {
    this.idmService = idmService;
    this.roleService = roleService;
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, int groupId) {
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (sess.getPrincipal().getRoles().containsKey(Role.ADMIN)) {
      return true;
    }

    return sess.getPrincipal().getRoles().get(Role.FORM_MANAGER).contains(groupId);
  }

  @Override
  public boolean canDecide(RegistrarAuthenticationToken sess, int groupId) {
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (sess.getPrincipal().getRoles().containsKey(Role.ADMIN)) {
      return true;
    }
//    return canManage(sess, groupId) || sess.getPrincipal().getRoles().get(Role.FORM_APPROVER).contains(groupId);
    return sess.getPrincipal().getRoles().get(Role.FORM_APPROVER).contains(groupId);
  }

}
