package org.perun.registrarprototype.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationRejectedEvent extends ApplicationEvent {

  public ApplicationRejectedEvent(Application application) {
    super(application);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
