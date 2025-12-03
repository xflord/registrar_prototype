package org.perun.registrarprototype.services.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationRejectedEvent extends ApplicationRelatedEvent {

  public ApplicationRejectedEvent(Application application) {
    super(application);
  }
}
