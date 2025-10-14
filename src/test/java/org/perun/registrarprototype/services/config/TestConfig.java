package org.perun.registrarprototype.services.config;

import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.AuthorizationService;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Forces the use of dummy auth implementation for testing.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {
  @Bean
  public AuthorizationService authorizationService() {
    return new AuthorizationServiceDummy();
  }

  @Bean
  public SessionProvider sessionProvider() {
    return new SessionProviderDummy();
  }

  @Bean
  @Primary
  public IdMService idMService() {
    return new IdMServiceDummy();
  }
}
