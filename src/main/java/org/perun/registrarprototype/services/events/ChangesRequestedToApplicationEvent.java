package org.perun.registrarprototype.services.events;

import org.perun.registrarprototype.models.Application;

public class ChangesRequestedToApplicationEvent extends ApplicationRelatedEvent {

  public ChangesRequestedToApplicationEvent(Application application) {
    super(application);
  }
}
