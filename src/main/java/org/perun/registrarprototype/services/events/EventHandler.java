package org.perun.registrarprototype.services.events;

import org.perun.registrarprototype.services.AuditingService;
import org.perun.registrarprototype.services.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// TODO should we do this asynchronously? Alternatively, individual handlers can be marked as async (or the methods they call)
//  correlation-id has to be stored as we emit to event if we decide to async handle events (since MDC is ThreadLocal)
@Component
public class EventHandler {

  private final AuditingService auditingService;
  private final NotificationService notificationService;

  public EventHandler(AuditingService auditingService,
                      NotificationService notificationService) {
    this.auditingService = auditingService;
    this.notificationService = notificationService;
  }

  @EventListener
  public void handleEvent(RegistrarEvent event) {
    System.out.println("Generic event handling for: " + event);
  }

  @EventListener(condition = "#event.auditable")
  public void auditEvent(RegistrarEvent event) {
    // TODO audit should include actor, so we need to pass user context (since this MIGHT be handled in a separate thread?)
    this.auditingService.logEvent(event);
  }

  @EventListener(condition = "#event.notifiable")
  public void notifyEvent(RegistrarEvent event) {
    this.notificationService.consoleNotificationService(event);
  }

  @EventListener
  public void handleApplicationEvent(ApplicationRelatedEvent event) {
    System.out.println("Application related event: " + event);
  }
}
