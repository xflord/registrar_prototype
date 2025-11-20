package org.perun.registrarprototype.services.impl;

import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.persistance.RoleRepository;
import org.perun.registrarprototype.services.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;

  public RoleServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public Map<Role, Set<String>> getRolesByUserId(String userId) {
    return roleRepository.getRolesByUserId(userId);
  }
}
