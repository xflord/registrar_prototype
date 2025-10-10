package org.perun.registrarprototype.services.idmIntegration.perun;

import cz.metacentrum.perun.openapi.PerunException;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Just a wrapper for some better exception handling.
 */
public class PerunRuntimeException extends RuntimeException {
  private final String errorId;
  private final String name;

  public PerunRuntimeException(PerunException ex) {
    super(ex.getMessage(), ex.getCause());
    this.name = ex.getName();
    this.errorId = ex.getErrorId();
  }

  public static PerunRuntimeException to(HttpClientErrorException ex) {
    return new PerunRuntimeException(PerunException.to(ex));
  }

  public String getErrorId() {
    return errorId;
  }

  public String getName() {
    return name;
  }
}
