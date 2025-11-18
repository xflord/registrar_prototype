package org.perun.registrarprototype.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.services.RoleService;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserLookupService {
  private final WebClient webClient = WebClient.create();
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Value( "${idp.user-info-uri}")
  private String userInfoEndpoint; // TODO more IDPs will be present to userinfo either has to be emitted or retrieved from the openid config
  private final IdMService idmService;
  private final RoleService roleService;

  public UserLookupService(IdMService idmService, RoleService roleService) {
    this.idmService = idmService;
    this.roleService = roleService;
  }
  // ideally eliminate this by including all in jwt
  @Cacheable(value = "userInfo", key = "#jwt.tokenValue")
  public Map<String, Object> getUserInfo(Jwt jwt) {
    try {
      String response = webClient.get()
          .uri(userInfoEndpoint)
          .header("Authorization", "Bearer " + jwt.getTokenValue())
          .retrieve()
          .bodyToMono(String.class)
          .block();

      return objectMapper.readValue(response, Map.class);
    } catch (Exception e) {
      System.err.println("Error getting user info: " + e.getMessage());
      return null;
    }
  }

  @Cacheable(
      value = "perunUserId",
      key = "#jwt.subject",
      unless = "#result == null " // do not cache for users not in Perun, we want to check every request in case they consolidated
  )
  public String perunUserData(Jwt jwt) {
    return idmService.getUserIdByIdentifier(jwt.getIssuer().toString(), jwt.getSubject());
  }

  // TODO refactor the MEMBERSHIP role (remove probs)
//  @Cacheable(value = "roles", key = "#sess.principal.attribute('sub')")
  public Map<Role, Set<String>> refreshAuthz(RegistrarAuthenticationToken sess) {
    if (!sess.isAuthenticated()) {
      return new HashMap<>(Map.of(Role.MEMBERSHIP, Set.of(), Role.FORM_MANAGER, Set.of(), Role.FORM_APPROVER, Set.of()));
    }
    Map<Role, Set<String>> roles = roleService.getRolesByUserId(sess.getPrincipal().id());
    roles.putIfAbsent(Role.MEMBERSHIP, new HashSet<>());
    Map<Role, Set<String>> rolesFromIdM = new HashMap<>();
    try {
      rolesFromIdM = idmService.getRolesByUserId(sess.getPrincipal().id());
    } catch (Exception e) {
      System.err.println("Error getting roles from IDM: " + e.getMessage());
      throw e;
      // do nothing
    }
    roles.get(Role.FORM_MANAGER).addAll(rolesFromIdM.getOrDefault(Role.FORM_MANAGER, Set.of()));
    roles.get(Role.FORM_APPROVER).addAll(rolesFromIdM.getOrDefault(Role.FORM_APPROVER, Set.of()));
    if (rolesFromIdM.containsKey(Role.ADMIN)) {
      roles.putIfAbsent(Role.ADMIN, Set.of());
    }
    roles.get(Role.MEMBERSHIP).addAll(rolesFromIdM.getOrDefault(Role.MEMBERSHIP, Set.of()));

    return roles;
  }
}
