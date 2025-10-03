package org.perun.registrarprototype.core.services;

import org.perun.registrarprototype.core.events.Event;
import org.perun.registrarprototype.core.services.tempImpl.AuditingServiceDummy;
import org.perun.registrarprototype.core.services.tempImpl.NotificationServiceDummy;
import org.springframework.stereotype.Component;

@Component
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
