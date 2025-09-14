package org.perun.registrarprototype.services;

import org.perun.registrarprototype.events.Event;

public interface AuditingService {
  void logEvent(Event event);
}
