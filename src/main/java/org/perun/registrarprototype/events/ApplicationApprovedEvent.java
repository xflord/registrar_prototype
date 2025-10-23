package org.perun.registrarprototype.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationApprovedEvent extends ApplicationEvent {

  public ApplicationApprovedEvent(Application application) {
    super(application);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
