package org.perun.registrarprototype.services.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationVerifiedEvent extends ApplicationRelatedEvent {

  public ApplicationVerifiedEvent(Application application) {
    super(application);
  }
}
