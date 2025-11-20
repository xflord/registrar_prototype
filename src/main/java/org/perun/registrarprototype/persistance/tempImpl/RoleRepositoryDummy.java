package org.perun.registrarprototype.persistance.tempImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.persistance.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class RoleRepositoryDummy implements RoleRepository {
  private static final Map<String, Map<Role, Set<String>>> rolesByUserId = new HashMap<>();

  @Override
  public Map<Role, Set<String>> getRolesByUserId(String userId) {
    return rolesByUserId.getOrDefault(userId, new HashMap<>(Map.of(Role.FORM_APPROVER, new HashSet<>(),
        Role.FORM_MANAGER, new HashSet<>())));
  }
}
