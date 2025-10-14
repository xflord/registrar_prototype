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
        token = "eyJraWQiOiJyc2ExIiwidHlwIjoiYXQrand0IiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiI0MTE0NzFkOS01ZTNkLTQ4YmQtOTcxNy0yNWYxZmE2MzRkMjAiLCJzdWIiOiIwNjdkMDA0ODc3YjExOGVlOGVjZTljNDUwYTU0OTliMDg0ZGIzMGQ4QGVpbmZyYS5jZXNuZXQuY3oiLCJhY3IiOiJodHRwczovL3JlZmVkcy5vcmcvcHJvZmlsZS9tZmEiLCJzY29wZSI6Im9wZW5pZCBvZmZsaW5lX2FjY2VzcyBwcm9maWxlIHBlcnVuX2FkbWluIHBlcnVuX2FwaSIsImF1dGhfdGltZSI6MTc2MDM0MTAyOSwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5lLWluZnJhLmN6L29pZGMvIiwiZXhwIjoxNzYwNDQ1OTgxLCJpYXQiOjE3NjA0NDIzODEsImNsaWVudF9pZCI6IjQxMTQ3MWQ5LTVlM2QtNDhiZC05NzE3LTI1ZjFmYTYzNGQyMCIsImp0aSI6IjE3ZmEzNzM2LTVhNDYtNGRhYS05ZjgyLTA0YWJkMzQ2ZDEyYiJ9.I1lx-7H4A8oOuxW4A4iZW1gw08EsvE5lLaXjzE4G1tz0Hk6ilCJIs4QQMiLYhXAiRc5uxZFMLQgYt2veYv093_x-kmmAGZLeQWnbCHrx8j80gF1HpFDF6RaqOgecAe_M0j98_p9-oq3m_BhNCHtzeeT4Q5BNfzc0B6Xzs6DppnAhpQAGLwnF97vESHtFyLb9GXVB-oP67mTPVLl2VCKHCbjgDzdHVxfuR8TB2zbdmn3gdyF_OyeQpk3YceGiGT4XyxoYhFazZGnmuwYm-7k2wRUL99it6oJCy1tvuLydnGBuiCElv3-DGHJdQtzCMizEEkl-Fmzf2O3VhqZNvtXh3g";
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        // TODO some session cookie handling can be done here
        return execution.execute(request, body);
    }
}