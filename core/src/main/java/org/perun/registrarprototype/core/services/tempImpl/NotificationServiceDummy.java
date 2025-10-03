package org.perun.registrarprototype.core.services.tempImpl;

import org.perun.registrarprototype.core.events.Event;
import org.perun.registrarprototype.core.services.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceDummy implements NotificationService {

  @Override
  public void consoleNotificationService(Event event) {
    System.out.println("Emitting to notification service - " + event.getEventName() + ": " + event.getEventMessage());
  }
}
