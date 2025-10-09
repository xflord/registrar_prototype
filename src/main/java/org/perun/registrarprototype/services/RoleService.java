package org.perun.registrarprototype.services;

import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;

/**
 * serves to handle registrar-specific roles
 */
public interface RoleService {
  Map<Role, Set<Integer>> getRolesByUserId(int userId);
}
