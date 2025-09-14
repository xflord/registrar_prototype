package org.perun.registrarprototype.services;

import org.perun.registrarprototype.events.Event;

public class NotificationServiceDummy implements NotificationService {

  @Override
  public void consoleNotificationService(Event event) {
    System.out.println("Emitting to notification service - " + event.getEventName() + ": " + event.getEventMessage());
  }
}
