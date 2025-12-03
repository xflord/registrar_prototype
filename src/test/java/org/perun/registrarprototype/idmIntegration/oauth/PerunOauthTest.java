package org.perun.registrarprototype.idmIntegration.oauth;

import cz.metacentrum.perun.openapi.PerunRPC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.persistence.FormSpecificationRepository;
import org.perun.registrarprototype.services.EventService;
import org.perun.registrarprototype.services.idmIntegration.perun.PerunIdMService;
import org.perun.registrarprototype.services.idmIntegration.perun.oauth.BearerTokenInterceptor;
import org.perun.registrarprototype.services.idmIntegration.perun.oauth.ClientAccessTokenService;
import org.perun.registrarprototype.persistence.ItemDefinitionRepository;
import org.perun.registrarprototype.persistence.DestinationRepository;
import org.perun.registrarprototype.persistence.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mockito;

@RestClientTest
@Import(TestOauthConfig.class)
@ActiveProfiles({"test", "oauth"})
public class PerunOauthTest {
  private final static String IDM_URL = "http://localhost:8080/idm";

  @Autowired
  private RestTemplateBuilder restTemplateBuilder;

  @Autowired
  private MockRestServiceServer mockServer;

  @Autowired
  private ClientAccessTokenService clientAccessTokenService;

  @Autowired
  private EventService eventService;

  private PerunRPC perunRpc;

  @BeforeEach
  public void setUp() {
      // interceptor to set access token in header for each request
      RestTemplate restTemplate = restTemplateBuilder.additionalInterceptors(new BearerTokenInterceptor(clientAccessTokenService)).build();

      this.perunRpc = new PerunRPC(restTemplate);
      this.perunRpc.getApiClient().setBasePath(IDM_URL);
  }

  @Test
  public void testTokenIsAddedToRequest() throws Exception {
    mockServer.expect(MockRestRequestMatchers.requestTo(IDM_URL + "/json/usersManager/getUserByExtSourceNameAndExtLogin" + "?extLogin=test-id&extSourceName=test-ext-source"))
          .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
          .andExpect(MockRestRequestMatchers.header("Authorization", "Bearer fake-access-token"))
          .andRespond(MockRestResponseCreators.withSuccess());

    ItemDefinitionRepository itemDefinitionRepository = Mockito.mock(ItemDefinitionRepository.class);
    DestinationRepository destinationRepository = Mockito.mock(DestinationRepository.class);
    FormSpecificationRepository formRepository = Mockito.mock(FormSpecificationRepository.class);
    SubmissionRepository submissionRepository = Mockito.mock(SubmissionRepository.class);
    
    PerunIdMService perunIdMService = new PerunIdMService(perunRpc, eventService, itemDefinitionRepository, destinationRepository, formRepository, submissionRepository);
    perunIdMService.getUserIdByIdentifier("test-ext-source", "test-id");

    mockServer.verify();
  }
}
