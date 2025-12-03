package org.perun.registrarprototype.services.tempImpl;

import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.perun.registrarprototype.services.AuditingService;
import org.springframework.stereotype.Service;

@Service
public class AuditingServiceDummy implements AuditingService {

    @Override
  public void logEvent(RegistrarEvent event) {
    // save event for auditing/monitoring
    System.out.println("Logging event - " + event.getEventMessage());
  }
}
