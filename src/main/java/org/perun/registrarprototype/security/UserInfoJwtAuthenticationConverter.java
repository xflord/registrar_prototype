package org.perun.registrarprototype.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class UserInfoJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Autowired
  private UserLookupService userLookupService;

  public UserInfoJwtAuthenticationConverter() {}

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    // Base attributes from JWT
    Map<String, Object> claims = new HashMap<>(jwt.getClaims());

    // Optional: call userinfo
    Map<String, Object> userInfo = userLookupService.getUserInfo(jwt);
    if (userInfo != null) {
      claims.putAll(userInfo);
    }

    String perunUserId = userLookupService.perunUserData(jwt);

    // TODO do we get groups from `entitlements`, or do we get roles from perun/group memberships?

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

    principal.setRoles(userLookupService.refreshAuthz(token));

    token.setCredentials(jwt.getTokenValue());
    return token;
  }
}
