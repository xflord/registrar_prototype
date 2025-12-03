package org.perun.registrarprototype.persistence.jdbc.crud;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.persistence.jdbc.entities.ItemDefinitionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDefinitionJdbcCrudRepository extends CrudRepository<ItemDefinitionEntity, Integer> {
  Optional<ItemDefinitionEntity> findByDisplayName(String displayName);
  List<ItemDefinitionEntity> findByGlobalTrue();
  List<ItemDefinitionEntity> findByFormSpecificationId(Integer formSpecificationId);
}
