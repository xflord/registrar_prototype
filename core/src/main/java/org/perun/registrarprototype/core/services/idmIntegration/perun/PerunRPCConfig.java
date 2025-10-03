package org.perun.registrarprototype.core.services.idmIntegration.perun;

import cz.metacentrum.perun.openapi.PerunRPC;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.perun.registrarprototype.core.services.idmIntegration.perun.oauth.BearerTokenInterceptor;
import org.perun.registrarprototype.core.services.idmIntegration.perun.oauth.ClientAccessTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PerunRPCConfig {
  @Value("${idm.url}")
  private String idmUrl;
  @Value("${idm.username}")
  private String idmUsername;
  @Value("${idm.password}")
  private String idmPassword;

  @Value("${idm.ssl.keystore-password}")
  private String keyStorePassword;
  @Value("${idm.ssl.key-password}")
  private String keyPassword;
  @Value("${idm.ssl.truststore-password}")
  private String trustStorePassword;

  // Bean for Basic Authentication
  @Bean
  @Profile("basic-auth")
  public PerunRPC perunRpcBasicAuth() {
      return new PerunRPC(idmUrl, idmUsername, idmPassword);
  }

  // Bean for X.509 Certificate Authentication
  // Note: this is not tested, use at your own risk!
  @Bean
  @Profile("x509")
  public PerunRPC perunRpcX509() throws Exception {

      PerunRPC certRpc = new PerunRPC(generateCertRestTemplate());
      certRpc.getApiClient().setBasePath(idmUrl);
      return certRpc;
  }

  private RestTemplate generateCertRestTemplate() throws Exception {
    SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(
                    // TODO encrypt all the passwords in vault
                        new ClassPathResource("client-keystore.p12").getFile(),   // path to client keystore
                        keyStorePassword.toCharArray(),  // keystore password
                        keyPassword.toCharArray()        // key password
                )
                .loadTrustMaterial(
                        new ClassPathResource("truststore.jks").getFile(),        // truststore with server CA
                        trustStorePassword.toCharArray()
                )
                .build();


    // Not sure if this is the correct approach, take this as a sample implementation (cert authentication might not even be used)
    HttpClientConnectionManager
        connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                                .setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext)).build();

    HttpClient httpClient = HttpClientBuilder
                        .create()
                        .setConnectionManager(connectionManager)
                        .build();

    HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory(httpClient);

    return new RestTemplate(factory);
  }

  // Bean for OAuth2 Authentication
  @Bean
  @Profile("oauth")
  public PerunRPC perunRpcOAuth(ClientAccessTokenService tokenService) {
      // Same as in PerunRPC
      RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
      // interceptor to set access token in header for each request
      restTemplate.setInterceptors(List.of(new BearerTokenInterceptor(tokenService)));

      PerunRPC oauthRpc = new PerunRPC(restTemplate);
      oauthRpc.getApiClient().setBasePath(idmUrl);
      oauthRpc.getApiClient().setBearerToken(tokenService.getAccessToken());
      return oauthRpc;
  }
}
