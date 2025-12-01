package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.persistance.jdbc.entities.FormItemEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FormItemJdbcCrudRepository extends CrudRepository<FormItemEntity, Integer> {
  List<FormItemEntity> findByFormId(Integer formId);
  List<FormItemEntity> findByItemDefinitionId(Integer itemDefinitionId);
  Optional<FormItemEntity> findById(Integer id);
  
  @Query("SELECT fi.* FROM form_item fi " +
         "INNER JOIN item_definition id ON fi.item_definition_id = id.id " +
         "INNER JOIN destination d ON id.destination_id = d.id " +
         "WHERE d.urn = :urn")
  List<FormItemEntity> findByDestinationUrn(@Param("urn") String urn);
  
  @Modifying
  @Query("DELETE FROM form_item WHERE form_id = :formId")
  void deleteByFormId(@Param("formId") Integer formId);
}

