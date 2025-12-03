package org.perun.registrarprototype.services.tempImpl;

import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.perun.registrarprototype.services.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceDummy implements NotificationService {

  @Override
  public void consoleNotificationService(RegistrarEvent event) {
    System.out.println("Emitting to notification service - " + event.getEventMessage());
  }
}
