package org.perun.registrarprototype.services.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.events.Event;
import org.perun.registrarprototype.services.AuditingService;
import org.springframework.stereotype.Service;

@Service
public class AuditingServiceDummy implements AuditingService {
  private static List<Event> events = new ArrayList<>();

    @Override
  public void logEvent(Event event) {
    // save event for auditing/monitoring
    System.out.println("Logging event - " + event.getEventName() + ": " + event.getEventMessage());
  }
}
