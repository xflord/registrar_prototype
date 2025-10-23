package org.perun.registrarprototype.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationVerifiedEvent extends ApplicationEvent {

  public ApplicationVerifiedEvent(Application application) {
    super(application);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
