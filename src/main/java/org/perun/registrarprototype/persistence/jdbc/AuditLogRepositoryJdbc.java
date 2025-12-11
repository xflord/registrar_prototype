package org.perun.registrarprototype.persistence.jdbc;

import org.perun.registrarprototype.persistence.jdbc.crud.AuditLogCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.AuditEventEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class AuditLogRepositoryJdbc {

  private final AuditLogCrudRepository auditLogCrudRepository;

  public AuditLogRepositoryJdbc(AuditLogCrudRepository auditLogCrudRepository) {
    this.auditLogCrudRepository = auditLogCrudRepository;
  }


  public RegistrarEvent save(RegistrarEvent event) {
    AuditEventEntity entity = EntityMapper.toEntity(event);

    auditLogCrudRepository.save(entity);
    return event;
  }
}
