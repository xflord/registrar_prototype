package org.perun.registrarprototype.persistance.jdbc;

import java.util.Optional;
import org.perun.registrarprototype.persistance.jdbc.entities.FormSpecificationEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FormSpecificationJdbcRepository extends CrudRepository<FormSpecificationEntity, Integer> {
  Optional<FormSpecificationEntity> findByGroupId(String groupId);
}

