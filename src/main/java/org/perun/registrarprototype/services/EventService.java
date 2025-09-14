package org.perun.registrarprototype.services;

import org.perun.registrarprototype.events.Event;

public interface EventService {
  void emitEvent(Event event);
}
