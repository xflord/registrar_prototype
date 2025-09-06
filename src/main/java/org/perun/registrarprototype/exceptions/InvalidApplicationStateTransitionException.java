package org.perun.registrarprototype.exceptions;

import org.perun.registrarprototype.models.Application;

public class InvalidApplicationStateTransitionException extends RuntimeException {
  private final Application application;
  public InvalidApplicationStateTransitionException(String message, Application application) {
    super(message);
    this.application = application;
  }

  public Application getApplication() {
    return application;
  }
}
