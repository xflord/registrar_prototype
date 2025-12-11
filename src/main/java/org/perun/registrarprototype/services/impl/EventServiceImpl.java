package org.perun.registrarprototype.services.impl;

import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.perun.registrarprototype.services.EventService;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventServiceImpl implements EventService {

  private final ApplicationEventPublisher publisher;
  private final SessionProvider sessionProvider;

  public EventServiceImpl(ApplicationEventPublisher publisher, SessionProvider sessionProvider) {
    this.publisher = publisher;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public void emitEvent(RegistrarEvent event) {

    event.setActor(sessionProvider.getCurrentSession().getPrincipal());
    event.setCorrelationId(MDC.get("correlationId"));

    this.publisher.publishEvent(event);
  }
}
