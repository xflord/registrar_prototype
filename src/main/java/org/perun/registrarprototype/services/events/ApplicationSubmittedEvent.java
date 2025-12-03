package org.perun.registrarprototype.services.events;

import org.perun.registrarprototype.models.Application;

public class ApplicationSubmittedEvent extends ApplicationRelatedEvent {

  public ApplicationSubmittedEvent(Application application) {
    super(application);
    this.notifiable = true;
  }
}
