package org.perun.registrarprototype.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
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
  private String userInfoEndpoint;
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
      return null;
    }
  }

  @Cacheable(value = "perunUserId", key = "#jwt.subject")
  public int perunUserData(Jwt jwt) {
    Integer perunUserId = idmService.getUserIdByIdentifier(jwt.getSubject());
    return perunUserId == null ? -1 : perunUserId;
  }

  @Cacheable("roles")
  public void refreshAuthz(RegistrarAuthenticationToken sess) {
    if (!sess.isAuthenticated()) {
      return;
    }
    Map<Role, Set<Integer>> roles = roleService.getRolesByUserId(sess.getPrincipal().id());
    Map<Role, Set<Integer>> rolesFromIdM = new HashMap<>();
    try {
      rolesFromIdM = idmService.getRegistrarRolesByUserId(sess.getPrincipal().id());
    } catch (Exception e) {
      // do nothing
    }
    roles.get(Role.FORM_MANAGER).addAll(rolesFromIdM.getOrDefault(Role.FORM_MANAGER, Set.of()));
    roles.get(Role.FORM_APPROVER).addAll(rolesFromIdM.getOrDefault(Role.FORM_APPROVER, Set.of()));
    if (rolesFromIdM.containsKey(Role.ADMIN)) {
      roles.putIfAbsent(Role.ADMIN, Set.of());
    }

    sess.getPrincipal().setRoles(roles);
  }
}
