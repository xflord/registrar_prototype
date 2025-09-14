package org.perun.registrarprototype.services;

import org.perun.registrarprototype.events.Event;

public class AuditingServiceDummy implements AuditingService{
  @Override
  public void logEvent(Event event) {
    // save event for auditing/monitoring
    System.out.println("Logging event - " + event.getEventName() + ": " + event.getEventMessage());
  }
}
