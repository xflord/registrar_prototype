package org.perun.registrarprototype.idmIntegration.oauth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.perun.registrarprototype.services.EventService;
import org.perun.registrarprototype.services.impl.EventServiceImpl;
import org.perun.registrarprototype.services.idmIntegration.perun.oauth.ClientAccessTokenService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test & oauth"})
public class TestOauthConfig {
  @Bean
  @Primary
  public ClientAccessTokenService mockClientAccessTokenService() {
    ClientAccessTokenService mock = mock(ClientAccessTokenService.class);
    when(mock.getAccessToken()).thenReturn("fake-access-token");
    return mock;
  }

  @Bean
  public EventService eventService(ApplicationEventPublisher publisher) {
    return new EventServiceImpl(publisher);
  }
}
