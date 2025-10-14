package org.perun.registrarprototype.services.idmIntegration.perun.oauth;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;

@Service
@Profile( "oauth")
public class ClientAccessTokenService {
    private final OAuth2AuthorizedClientManager manager;
    private final Authentication principal;

    public ClientAccessTokenService(ClientRegistrationRepository registrations,
		OAuth2AuthorizedClientRepository authorizedClientRepositor) {

        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        DefaultOAuth2AuthorizedClientManager m =
                new DefaultOAuth2AuthorizedClientManager(registrations, authorizedClientRepositor);

        m.setAuthorizedClientProvider(provider);
        this.manager = m;

        // Dummy principal for client_credentials flow
        this.principal = new AnonymousAuthenticationToken(
                "system", "system", AuthorityUtils.createAuthorityList("ROLE_SYSTEM"));
    }

    public String getAccessToken() {
      return "";
    }


//    public String getAccessToken() {
//        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
//                .withClientRegistrationId("core-api-client")
//                .principal(principal)
//                .build();
//
//        OAuth2AuthorizedClient client = manager.authorize(request);
//        if (client == null || client.getAccessToken() == null) {
//            throw new IllegalStateException("Could not obtain access token");
//        }
//        return client.getAccessToken().getTokenValue();
//    }
}
