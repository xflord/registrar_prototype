package org.perun.registrarprototype.persistance.jdbc;

import java.util.Optional;
import org.perun.registrarprototype.persistance.jdbc.entities.ScriptModuleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptModuleJdbcCrudRepository extends CrudRepository<ScriptModuleEntity, Integer> {
  Optional<ScriptModuleEntity> findByName(String name);
}