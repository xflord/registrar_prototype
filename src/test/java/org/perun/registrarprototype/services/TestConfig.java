package org.perun.registrarprototype.services;

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
}
