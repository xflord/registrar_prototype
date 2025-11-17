package org.perun.registrarprototype.services.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.security.SessionProvider;
import org.springframework.security.core.GrantedAuthority;

public class SessionProviderDummy extends SessionProvider {
  @Override
  public RegistrarAuthenticationToken getCurrentSession() {
    String id = "-1";

    Set<String> managedGroups = new HashSet<>();

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", id);
    attributes.put("testAttribute", "testValue");

    List<GrantedAuthority> roles = new ArrayList<>();

    CurrentUser principal = new CurrentUser(id, managedGroups, attributes);
    return new RegistrarAuthenticationToken(principal, roles);
  }
}
