package org.perun.registrarprototype.services;

import org.perun.registrarprototype.events.Event;
import org.perun.registrarprototype.services.tempImpl.AuditingServiceDummy;
import org.perun.registrarprototype.services.tempImpl.NotificationServiceDummy;

public class EventServiceImpl implements EventService {

  private final NotificationService notificationService = new NotificationServiceDummy();
  private final AuditingService auditingService = new AuditingServiceDummy();

  public EventServiceImpl() {
  }

  @Override
  public void emitEvent(Event event) {
    this.notificationService.consoleNotificationService(event);
    this.auditingService.logEvent(event);
  }
}
