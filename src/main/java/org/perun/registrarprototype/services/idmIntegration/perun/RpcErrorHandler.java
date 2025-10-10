package org.perun.registrarprototype.services.idmIntegration.perun;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

public class RpcErrorHandler extends DefaultResponseErrorHandler {

  @Override
  protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode, URI url, HttpMethod method)
      throws IOException {
    try {
      super.handleError(response, statusCode, url, method);
    } catch (HttpClientErrorException ex) {
      throw PerunRuntimeException.to(ex);
    }
  }
}

