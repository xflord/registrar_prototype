package org.perun.registrarprototype.services.idmIntegration.perun.oauth;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpHeaders;
import java.io.IOException;

@Profile( "oauth" )
public class BearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private final ClientAccessTokenService tokenService;

    public BearerTokenInterceptor(ClientAccessTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = tokenService.getAccessToken();
      if (token != null) {
        token = "eyJraWQiOiJyc2ExIiwidHlwIjoiYXQrand0IiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiI0MTE0NzFkOS01ZTNkLTQ4YmQtOTcxNy0yNWYxZmE2MzRkMjAiLCJzdWIiOiIwNjdkMDA0ODc3YjExOGVlOGVjZTljNDUwYTU0OTliMDg0ZGIzMGQ4QGVpbmZyYS5jZXNuZXQuY3oiLCJhY3IiOiJodHRwczovL3JlZmVkcy5vcmcvcHJvZmlsZS9tZmEiLCJzY29wZSI6Im9wZW5pZCBvZmZsaW5lX2FjY2VzcyBwcm9maWxlIHBlcnVuX2FkbWluIHBlcnVuX2FwaSIsImF1dGhfdGltZSI6MTc2MDM0MTAyOSwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5lLWluZnJhLmN6L29pZGMvIiwiZXhwIjoxNzYwMzYzOTQwLCJpYXQiOjE3NjAzNjAzNDAsImNsaWVudF9pZCI6IjQxMTQ3MWQ5LTVlM2QtNDhiZC05NzE3LTI1ZjFmYTYzNGQyMCIsImp0aSI6ImE5Yjk5ZTgwLTdkM2MtNGIzZS04YTkwLTkxNjVlMjk0YTg1YiJ9.X8dg9ZOPnZQWEUYC9f2SAFmoCFJJ5InjCN4I8Frir7QRNdnGsJGsLVksdgjuciebmEzDsODiCK6xi1YYDXtIi2KtUJnZsilldFgk2Cfgmq1m2QQTiofX68VR-BEI8JWLKrTYfgIZYvtFupcmJKCkUmIyOrhHIQxeMDYQNNluV4pz-1HcusRdjU9demc3hnrfU7Xnegq6S3jes0XI8VmlSD0XLRRLxfJaIAeI2Q-vWba2beF1D47KJn894LW6F0vFSExl41w8XhhzQq8LgCkxjC5KHTXEKWs79QNO1-yjO5bhE9wdkoen1_DU5OWtPbeNHazi6mVQ1ZVLRkbQnh4-dQ";
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        // TODO some session cookie handling can be done here
        return execution.execute(request, body);
    }
}