package org.perun.registrarprototype.services.events;

import org.perun.registrarprototype.models.Application;

public abstract class ApplicationRelatedEvent extends RegistrarEvent {
  protected Application application;

  protected ApplicationRelatedEvent(Application application) {
    this.application = application;
    this.auditable = true; // do it this way if we do not expect auditable/notifiable to be computed
  }

  @Override
  public boolean isAuditable() {
    // return application.isCritical()
    return true; // do it this way if we expect some events to be conditionally auditable/notifiable
  }

  public Application getApplication() {
    return application;
  }

  public String getEventMessage() {
    return this.formatEventMessage(application);
  }
}
