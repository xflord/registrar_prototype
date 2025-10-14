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
        token = "eyJraWQiOiJyc2ExIiwidHlwIjoiYXQrand0IiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiI0MTE0NzFkOS01ZTNkLTQ4YmQtOTcxNy0yNWYxZmE2MzRkMjAiLCJzdWIiOiIwNjdkMDA0ODc3YjExOGVlOGVjZTljNDUwYTU0OTliMDg0ZGIzMGQ4QGVpbmZyYS5jZXNuZXQuY3oiLCJhY3IiOiJodHRwczovL3JlZmVkcy5vcmcvcHJvZmlsZS9tZmEiLCJzY29wZSI6Im9wZW5pZCBvZmZsaW5lX2FjY2VzcyBwcm9maWxlIHBlcnVuX2FkbWluIHBlcnVuX2FwaSIsImF1dGhfdGltZSI6MTc2MDM0MTAyOSwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5lLWluZnJhLmN6L29pZGMvIiwiZXhwIjoxNzYwNDUzOTE0LCJpYXQiOjE3NjA0NTAzMTQsImNsaWVudF9pZCI6IjQxMTQ3MWQ5LTVlM2QtNDhiZC05NzE3LTI1ZjFmYTYzNGQyMCIsImp0aSI6IjgzYzZhYmNlLTAyM2QtNDhhMS04NzFmLTQ0NzliOTFmZjEyOCJ9.c4d5ZhnnwgPxKGeKF1axGNuqK5_NtfJL5gKmFUSXWvUbuxnfPY0NNmJpSAhvszj7-NkFsJmb6rutaB2vlBqmS83zzdK3xAKd1jZua_sDyoQiZxKwQrKktWGCoe2nnoTT1fc8SUk4t1kbiABR8MoTw6zYTjDaoZT2EFbtCPjm_qTV9XUVPCZ0_TcxTXpkanlmG1wO33kcxnP_YxZq5J-kvwrk7SC-6DfCa2BzDXrLucXlmRyCQ5hZA5xWHLxil89dzZljBAfsAEOjdTWE4cUbCHr-UCGdv0-lIzR3xKWD8vuES_WF1Bpb314_HAA6yLXrdlJglkndcVtzghqNETbv2w";
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        // TODO some session cookie handling can be done here
        return execution.execute(request, body);
    }
}