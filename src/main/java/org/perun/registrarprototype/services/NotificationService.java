package org.perun.registrarprototype.services;

import org.perun.registrarprototype.services.events.RegistrarEvent;

public interface NotificationService {
  void consoleNotificationService(RegistrarEvent event);
}
