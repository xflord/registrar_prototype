package org.perun.registrarprototype.core.security;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimAccessor;

/**
 * Enriches the default OAuth2AuthenticatedPrincipal with additional claims.
 * The additional claims from UserInfo can be used to autofill forms.
 */
public class UserInfoEnrichedPrincipal implements OAuth2AuthenticatedPrincipal, OAuth2TokenIntrospectionClaimAccessor {
  // DefaultOAuth2AuthenticatedPrincipal by default
  private final OAuth2AuthenticatedPrincipal delegate;

  public UserInfoEnrichedPrincipal(Map<String, Object> attributes, Collection<GrantedAuthority> authorities) {
    this.delegate = new DefaultOAuth2AuthenticatedPrincipal(attributes, authorities);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return delegate.getAttributes();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return delegate.getAuthorities();
  }

  @Override
  public String getName() {
    Object name = this.getAttributes().get("name");
    return name == null ? delegate.getName() : (String) name;
  }

  @Override
  public Map<String, Object> getClaims() {
    return this.getAttributes();
  }
}
