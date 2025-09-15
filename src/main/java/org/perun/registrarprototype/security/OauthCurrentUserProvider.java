package org.perun.registrarprototype.security;

import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.context.SecurityContextHolder;

public class OauthCurrentUserProvider implements CurrentUserProvider {
  @Override
  public CurrentUser getCurrentUser(String authHeader) {
    UserInfoEnrichedPrincipal principal = (UserInfoEnrichedPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    int userId = principal.getAttributes().get("sub") == null ? -1 : Integer.parseInt(principal.getAttributes().get("sub").toString());
    // TODO use the retrieved userId to fetch registrar roles from the database (and implement the roles),
    //  though this can be done while initially building the principal in the introspector

    Set<Integer> managedGroups = new HashSet<>();

    String groupsClaim = principal.getAttributes().get("entitlements") == null ? "" : principal.getAttributes().get("entitlements").toString();
    for (String group : groupsClaim.split(",")) {
      // example entitlement: urn:perun:group:einfra:managers
      // TODO find out how to get the group id from the urn
      managedGroups.add(Integer.parseInt(group));
    }

    if (userId != -1) {
      return new CurrentUser(userId, managedGroups);
    }

    return null;
  }
}
