package org.perun.registrarprototype.security;

public interface CurrentUserProvider {
  CurrentUser getCurrentUser(String authHeader);
}
