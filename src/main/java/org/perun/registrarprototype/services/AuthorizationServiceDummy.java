package org.perun.registrarprototype.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.perun.registrarprototype.models.CurrentUser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthorizationServiceDummy implements AuthorizationService {
  @Override
  public boolean canApprove(int applicationId) {
    return true;
  }

  // temporary testing IDM principal retrieval (either replace with own principal creation + role management or implement provider + caching)
  // this would result in coupling, harder for kubernetes. Can still be simplified by spring security and filtering pre-call,
  // What are the options if we want async calls to core? Initial load of managers, then audit event polling to hold own list of managers?
  @Override
  public CurrentUser fetchPrincipal(String authHeader) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, authHeader);

    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://api-dev.perun-aai.org/oauth/rpc/json/authzResolver/getPerunPrincipal",
        HttpMethod.GET,
        request,
        String.class
    );
    try {
      JsonNode root = new ObjectMapper().readTree(response.getBody());

      int userId = root.path("user").path("id").asInt();
      JsonNode managedGroupsNode = root.path("roles").path("GROUPADMIN").path("Group");
      Set<Integer> managedGroups = new HashSet<>();
      if (managedGroupsNode.isArray()) {
        for (JsonNode groupNode : managedGroupsNode) {
          managedGroups.add(groupNode.asInt());
        }
      }

      return new CurrentUser(userId,  managedGroups);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
