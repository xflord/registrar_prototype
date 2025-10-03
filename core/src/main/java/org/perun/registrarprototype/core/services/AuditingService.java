package org.perun.registrarprototype.core.services;

import org.perun.registrarprototype.core.events.Event;

public interface AuditingService {
  void logEvent(Event event);
}
