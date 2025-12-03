package org.perun.registrarprototype.services.impl;

import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.perun.registrarprototype.services.EventService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventServiceImpl implements EventService {

  private final ApplicationEventPublisher publisher;

  public EventServiceImpl(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public void emitEvent(RegistrarEvent event) {
    this.publisher.publishEvent(event);
  }
}
