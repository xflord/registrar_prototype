package org.perun.registrarprototype.services.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationApprovedEvent extends ApplicationRelatedEvent {

  public ApplicationApprovedEvent(Application application) {
    super(application);
  }
}
