package org.perun.registrarprototype.security;

import cz.metacentrum.perun.openapi.model.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserInfoJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final String userInfoEndpoint;
  private final WebClient webClient = WebClient.create();
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired
  private IdMService idmService;

  public UserInfoJwtAuthenticationConverter(String userInfoEndpoint) {
    this.userInfoEndpoint = userInfoEndpoint;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    // Base attributes from JWT
    Map<String, Object> claims = new HashMap<>(jwt.getClaims());

    // Optional: call userinfo
    Map<String, Object> userInfo = getUserInfo(jwt.getTokenValue());
    if (userInfo != null) {
      claims.putAll(userInfo);
    }

    int perunUserId = perunUserData(jwt.getSubject());

    // Convert scopes to authorities
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    // should be fine to pass the original jwt here, userinfo should not add scopes
    Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt); // default behavior should be enough for our case
    //    Collection<GrantedAuthority> authorities = claims.containsKey("scope") ?
    //                                                   Arrays.stream(jwt.getClaimAsString("scope").split(" "))
    //                                                       .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
    //                                                       .collect(Collectors.toList()) : new ArrayList<>();

    // Create custom principal
    CurrentUser principal = new CurrentUser(perunUserId, new HashSet<>(), claims);

    // Create authentication
    RegistrarAuthenticationToken token = new RegistrarAuthenticationToken(principal, authorities);
    token.setCredentials(jwt.getTokenValue());
    return token;
  }

  // ideally eliminate this by including all in jwt
  private Map<String, Object> getUserInfo(String accessToken) {
    try {
      String response = webClient.get()
          .uri(userInfoEndpoint)
          .header("Authorization", "Bearer " + accessToken)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      return objectMapper.readValue(response, Map.class);
    } catch (Exception e) {
      return null;
    }
  }

  private int perunUserData(String subject) {
    User perunUser = idmService.getUserByIdentifier(subject);
    return perunUser == null ? -1 : perunUser.getId();
  }
}
