package org.perun.registrarprototype.events;

import org.perun.registrarprototype.models.Application;

public abstract class ApplicationEvent extends Event {
  protected Application application;

  protected ApplicationEvent(Application application) {
    this.application = application;
  }

  public Application getApplication() {
    return application;
  }
}
