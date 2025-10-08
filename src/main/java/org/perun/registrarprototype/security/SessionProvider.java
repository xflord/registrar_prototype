package org.perun.registrarprototype.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionProvider {

  public RegistrarAuthenticationToken getCurrentSession() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (RegistrarAuthenticationToken) authentication;
  }
}
