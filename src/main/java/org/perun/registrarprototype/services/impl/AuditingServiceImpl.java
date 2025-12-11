package org.perun.registrarprototype.services.impl;

import org.perun.registrarprototype.persistence.jdbc.AuditLogRepositoryJdbc;
import org.perun.registrarprototype.services.AuditingService;
import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditingServiceImpl implements AuditingService {

  private final static Logger LOG = LoggerFactory.getLogger(AuditingServiceImpl.class);
  private final AuditLogRepositoryJdbc auditLogRepository;

  public AuditingServiceImpl(AuditLogRepositoryJdbc auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Override
  @Async
  public void logEvent(RegistrarEvent event) {
    auditLogRepository.save(event);

    // TODO determine audit message format
    LOG.info("EVENT: {} | ACTOR: {} | MESSAGE {} ", event.getEventName(), event.getActor(), event.getEventMessage());
  }
}
