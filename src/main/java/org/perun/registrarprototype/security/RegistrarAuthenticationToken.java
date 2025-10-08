package org.perun.registrarprototype.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class RegistrarAuthenticationToken extends AbstractAuthenticationToken {
  private final CurrentUser principal;
  private String token;

  public RegistrarAuthenticationToken(CurrentUser principal, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    // In the authenticated flow (from the converter), this is set to true.
    // In the unauthenticated filter, we explicitly set it to false.
    setAuthenticated(true);
  }

  public void setCredentials(String credentials) {
    this.token = credentials;
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public CurrentUser getPrincipal() {
    return principal;
  }

  @Override
  public String getName() {
    // The getName() is usually the unique ID. For authenticated users,
    // it defaults to the 'sub' claim.
    // For unauthenticated users, it's the default ID (e.g., "0").
    return principal.id();
  }
}
