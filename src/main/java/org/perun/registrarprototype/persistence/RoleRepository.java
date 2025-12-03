package org.perun.registrarprototype.persistence;

import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;

public interface RoleRepository {

  Map<Role, Set<String>> getRolesByUserId(String userId);
}
