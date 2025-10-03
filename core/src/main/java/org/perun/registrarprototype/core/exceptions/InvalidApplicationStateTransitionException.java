package org.perun.registrarprototype.core.exceptions;

import org.perun.registrarprototype.core.models.Application;

public class InvalidApplicationStateTransitionException extends Exception {
  private final Application application;
  public InvalidApplicationStateTransitionException(String message, Application application) {
    super(message);
    this.application = application;
  }

  public Application getApplication() {
    return application;
  }
}
