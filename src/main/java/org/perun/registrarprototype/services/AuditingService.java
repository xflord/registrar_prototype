package org.perun.registrarprototype.services;

import org.perun.registrarprototype.services.events.RegistrarEvent;

public interface AuditingService {
  void logEvent(RegistrarEvent event);
}
