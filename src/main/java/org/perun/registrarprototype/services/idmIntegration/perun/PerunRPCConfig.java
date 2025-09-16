package org.perun.registrarprototype.services.idmIntegration.perun;

import cz.metacentrum.perun.openapi.PerunRPC;
import java.io.File;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.perun.registrarprototype.services.idmIntegration.perun.oauth.BearerTokenInterceptor;
import org.perun.registrarprototype.services.idmIntegration.perun.oauth.ClientAccessTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  // Bean for Basic Authentication
  @Bean
  @ConditionalOnProperty(name = "perun.auth.method", havingValue = "basic-auth")
  public PerunRPC perunRpcBasicAuth() {
      return new PerunRPC(idmUrl, idmUsername, idmPassword);
  }

  // Bean for X.509 Certificate Authentication
  // Note: this is not tested, use at your own risk!
  @Bean
  @ConditionalOnProperty(name = "perun.auth.method", havingValue = "x509")
  public PerunRPC perunRpcX509() throws Exception {

      PerunRPC certRpc = new PerunRPC(generateRestTemplate());
      certRpc.getApiClient().setBasePath(idmUrl);
      return certRpc;
  }

  private RestTemplate generateRestTemplate() throws Exception {
    SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(
                    // TODO encrypt all the passwords in vault
                        new File("client-keystore.p12"),   // path to client keystore
                        "keystorePassword".toCharArray(),  // keystore password
                        "keyPassword".toCharArray()        // key password
                )
                .loadTrustMaterial(
                        new File("truststore.jks"),        // truststore with server CA
                        "truststorePassword".toCharArray()
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
  @ConditionalOnProperty(name = "perun.auth.method", havingValue = "oauth")
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
