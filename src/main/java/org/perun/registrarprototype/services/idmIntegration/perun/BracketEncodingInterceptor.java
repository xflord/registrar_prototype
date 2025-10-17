package org.perun.registrarprototype.services.idmIntegration.perun;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

/**
 * Openapi generator does not support brackets in collection query parameters (which the Perun openapi spec uses, don't ask why).
 * Hence, the generated code encodes brackets in the query parameters. This interceptor decodes them.
 */
public class BracketEncodingInterceptor implements ClientHttpRequestInterceptor {
  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    URI originalURI = request.getURI();
    String fixedUrl = originalURI.toString().replace("%255B%255D", "[]");

    if (!fixedUrl.equals(originalURI.toString())) {
      HttpRequest fixedRequest = new HttpRequestWrapper(request) {
        @Override
        public URI getURI() {
          return URI.create(fixedUrl);
        }
      };
      return execution.execute(fixedRequest, body);
    }
    return execution.execute(request, body);
  }
}
