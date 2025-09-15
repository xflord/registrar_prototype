package org.perun.registrarprototype.services.tempImpl;

import org.perun.registrarprototype.events.Event;
import org.perun.registrarprototype.services.NotificationService;

public class NotificationServiceDummy implements NotificationService {

  @Override
  public void consoleNotificationService(Event event) {
    System.out.println("Emitting to notification service - " + event.getEventName() + ": " + event.getEventMessage());
  }
}
