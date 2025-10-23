package org.perun.registrarprototype.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationSubmittedEvent extends ApplicationEvent {

  public ApplicationSubmittedEvent(Application application) {
    super(application);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
