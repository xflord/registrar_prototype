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
        token = "eyJraWQiOiJyc2ExIiwidHlwIjoiYXQrand0IiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiI0MTE0NzFkOS01ZTNkLTQ4YmQtOTcxNy0yNWYxZmE2MzRkMjAiLCJzdWIiOiIwNjdkMDA0ODc3YjExOGVlOGVjZTljNDUwYTU0OTliMDg0ZGIzMGQ4QGVpbmZyYS5jZXNuZXQuY3oiLCJhY3IiOiJodHRwczovL3JlZmVkcy5vcmcvcHJvZmlsZS9tZmEiLCJzY29wZSI6Im9wZW5pZCBvZmZsaW5lX2FjY2VzcyBwcm9maWxlIHBlcnVuX2FkbWluIHBlcnVuX2FwaSIsImF1dGhfdGltZSI6MTc2MDQ1MzkyNiwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5lLWluZnJhLmN6L29pZGMvIiwiZXhwIjoxNzYwNDU5ODI2LCJpYXQiOjE3NjA0NTYyMjYsImNsaWVudF9pZCI6IjQxMTQ3MWQ5LTVlM2QtNDhiZC05NzE3LTI1ZjFmYTYzNGQyMCIsImp0aSI6IjM5MWM3NTlkLTEwNmMtNDU3MC04OWI4LTQ3ZDVhMWFjZWMyNSJ9.lzO9sKFK-NuFU4SQmtmv77QpFQfC3dQRWJeEaK80esPG4TEQzPR9p9AoX-5JBfZNGk06UyyGPG5dB-G2MM4gDM5H_UFCDk7qPATvXmoPG55Met2eyHdwCasPz1mZGmOZa1OWYb7heHntOYnUe0fNIU2A2BHUY--FRErpR9gkCrnlP0y10DO8sZjUVhFMFQxcFG3mgsac4-Mp_TBAv0UwnhA3YuR5EVRKnve-8cIIgrwxndK9Bf1keQOx83r-MJ-LPI1m3sdHL_D21HOHcHSRF4siG0FpvWxsAyzeOhoZIqkLpiX61IdQyo9sv7uhxLb5HiLof1K0qvoSvsDPRM-ggg";
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        // TODO some session cookie handling can be done here
        return execution.execute(request, body);
    }
}