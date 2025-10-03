package org.perun.registrarprototype.core.security;

public interface CurrentUserProvider {
  CurrentUser getCurrentUser(String authHeader);
}
