package org.perun.registrarprototype.persistence.jdbc.crud;

import java.util.Optional;
import org.perun.registrarprototype.persistence.jdbc.entities.FormSpecificationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormSpecificationJdbcCrudRepository extends CrudRepository<FormSpecificationEntity, Integer> {
  Optional<FormSpecificationEntity> findByGroupId(String groupId);
}

