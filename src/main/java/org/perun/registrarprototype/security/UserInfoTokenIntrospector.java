package org.perun.registrarprototype.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.web.reactive.function.client.WebClient;

public class UserInfoTokenIntrospector implements OpaqueTokenIntrospector {

  private final String userInfoEndpoint;
  private final OpaqueTokenIntrospector delegate;
  private final WebClient rest = WebClient.create();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public UserInfoTokenIntrospector(String introspectionUri, String clientId, String clientSecret, String userInfoEndPoint) {
    this.delegate = SpringOpaqueTokenIntrospector.withIntrospectionUri(introspectionUri).clientId(clientId).clientSecret(clientSecret).build();
    this.userInfoEndpoint = userInfoEndPoint;
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);

    System.out.println("Retrieved principal: " + principal.getName());

    Map<String, Object> attributes = getUserInfo(token);

    attributes.putAll(principal.getAttributes());

    return new UserInfoEnrichedPrincipal(attributes, (Collection<GrantedAuthority>) principal.getAuthorities());
  }

  private Map<String, Object> getUserInfo(String token) {
    String response = rest.get()
                          .uri(userInfoEndpoint)
                          .header("Authorization", "Bearer " + token)
                          .retrieve()
                          .bodyToMono(String.class)
                          .block();
    try {
      return objectMapper.readValue(response, Map.class);
    } catch (JsonProcessingException e) {
      return new HashMap<>();
    }
  }
}
