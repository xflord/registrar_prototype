package org.perun.registrarprototype.persistence.jdbc.crud;

import org.perun.registrarprototype.persistence.jdbc.entities.AuditEventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogCrudRepository extends CrudRepository<AuditEventEntity, Integer> {
}
