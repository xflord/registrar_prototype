package org.perun.registrarprototype.services;

import org.perun.registrarprototype.services.events.RegistrarEvent;

public interface EventService {
  void emitEvent(RegistrarEvent event);
}
