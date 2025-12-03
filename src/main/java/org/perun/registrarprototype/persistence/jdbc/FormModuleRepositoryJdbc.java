package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.persistence.FormModuleRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.AssignedFormModuleJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.AssignedFormModuleEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormModuleRepositoryJdbc implements FormModuleRepository {

  private final AssignedFormModuleJdbcCrudRepository jdbcRepository;

  public FormModuleRepositoryJdbc(AssignedFormModuleJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public List<AssignedFormModule> findAllByFormId(int formId) {
    return jdbcRepository.findByFormIdOrderByPosition(formId).stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void saveAll(List<AssignedFormModule> modules) {
    List<AssignedFormModuleEntity> entities = new ArrayList<>();
    for (int i = 0; i < modules.size(); i++) {
      AssignedFormModuleEntity entity = EntityMapper.toEntity(modules.get(i));
      entity.setPosition(i);
      entities.add(entity);
    }
    jdbcRepository.saveAll(entities);
  }
}