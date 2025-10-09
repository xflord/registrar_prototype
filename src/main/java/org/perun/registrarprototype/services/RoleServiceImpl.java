package org.perun.registrarprototype.services;

import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.repositories.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;

  public RoleServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public Map<Role, Set<Integer>> getRolesByUserId(int userId) {
    return roleRepository.getRolesByUserId(userId);
  }
}
