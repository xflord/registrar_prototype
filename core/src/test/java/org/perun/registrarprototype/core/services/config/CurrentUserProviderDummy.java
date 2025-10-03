package org.perun.registrarprototype.core.services.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.core.security.CurrentUser;
import org.perun.registrarprototype.core.security.CurrentUserProvider;
import org.perun.registrarprototype.core.security.UserInfoEnrichedPrincipal;
import org.springframework.security.core.GrantedAuthority;

public class CurrentUserProviderDummy implements CurrentUserProvider {
  @Override
  public CurrentUser getCurrentUser(String authHeader) {
    int id = -1;

    Set<Integer> managedGroups = new HashSet<>();

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", String.valueOf(id));
    attributes.put("testAttribute", "testValue");

    List<GrantedAuthority> roles = new ArrayList<>();

    UserInfoEnrichedPrincipal principal = new UserInfoEnrichedPrincipal(attributes, roles);

    return new CurrentUser(id, managedGroups, principal);
  }
}
