package org.perun.registrarprototype.repositories.tempImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.repositories.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class RoleRepositoryDummy implements RoleRepository {
  private static final Map<Integer, Map<Role, Set<Integer>>> rolesByUserId = Map.of();

  @Override
  public Map<Role, Set<Integer>> getRolesByUserId(int userId) {
    return rolesByUserId.getOrDefault(userId, new HashMap<>(Map.of(Role.FORM_APPROVER, new HashSet<>(),
        Role.FORM_MANAGER, new HashSet<>())));
  }
}
