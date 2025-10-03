package org.perun.registrarprototype.core.services.config;

import org.perun.registrarprototype.core.security.CurrentUserProvider;
import org.perun.registrarprototype.core.services.AuthorizationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Forces the use of dummy auth implementation for testing.
 */
@TestConfiguration
public class TestConfig {
  @Bean
  public AuthorizationService authorizationService() {
    return new AuthorizationServiceDummy();
  }

  @Bean
  public CurrentUserProvider currentUserProvider() {
    return new CurrentUserProviderDummy();
  }
}
