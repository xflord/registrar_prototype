package org.perun.registrarprototype.services;

import java.util.Objects;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.stereotype.Service;

// Role-based access control implementation
@Service
public class AuthorizationServiceImpl implements AuthorizationService {

  private final IdMService idmService;
  private final RoleService roleService;

  public AuthorizationServiceImpl(IdMService idmService, RoleService roleService) {
    this.idmService = idmService;
    this.roleService = roleService;
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, String groupId) {
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (isAdmin(sess)) {
      return true;
    }

    return sess.getPrincipal().getRoles().get(Role.FORM_MANAGER).contains(groupId);
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, Application app) {
    // TODO verify this is all the applicable conditions
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (isAdmin(sess)) {
      return true;
    }

    if (app.getIdmUserId() != null && Objects.equals(app.getIdmUserId(), sess.getPrincipal().id())) {
      return true;
    }

    return Objects.equals(app.getSubmission().getIdentityIssuer(), sess.getPrincipal().attribute("iss")) &&
               Objects.equals(app.getSubmission().getIdentityIdentifier(), sess.getPrincipal().attribute("sub"));
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, ItemDefinition itemDefinition) {
    if (itemDefinition.isGlobal()) {
      return isAdmin(sess);
    } else {
      if (itemDefinition.getDestination().isGlobal()) {
        return isAdmin(sess);
      }
      for (PrefillStrategyEntry entry : itemDefinition.getPrefillStrategies()) {
        if (entry.isGlobal()) {
          return isAdmin(sess);
        }
      }
      if (itemDefinition.getFormSpecification() == null) {
        throw new IllegalArgumentException("Form specification is null");
      }
      // Authorization check
      return canManage(sess, itemDefinition.getFormSpecification().getGroupId());
    }
  }

  @Override
  public boolean canDecide(RegistrarAuthenticationToken sess, String groupId) {
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (isAdmin(sess)) {
      return true;
    }
//    return canManage(sess, groupId) || sess.getPrincipal().getRoles().get(Role.FORM_APPROVER).contains(groupId);
    return sess.getPrincipal().getRoles().get(Role.FORM_APPROVER).contains(groupId);
  }

  @Override
  public boolean isAdmin(RegistrarAuthenticationToken sess) {
    return sess.getPrincipal().getRoles().containsKey(Role.ADMIN);
  }

}
