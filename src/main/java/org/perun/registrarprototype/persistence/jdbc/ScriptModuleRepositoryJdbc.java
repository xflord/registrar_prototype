package org.perun.registrarprototype.persistence.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.perun.registrarprototype.models.ScriptModule;
import org.perun.registrarprototype.persistence.ScriptModuleRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.ScriptModuleJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.ScriptModuleEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class ScriptModuleRepositoryJdbc implements ScriptModuleRepository {

  private final ScriptModuleJdbcCrudRepository jdbcRepository;

  public ScriptModuleRepositoryJdbc(
      ScriptModuleJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public List<ScriptModule> findAll() {
    Iterable<ScriptModuleEntity> entities = jdbcRepository.findAll();
    return StreamSupport.stream(entities.spliterator(), false)
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ScriptModule> findByName(String name) {
    return jdbcRepository.findByName(name)
        .map(DomainMapper::toDomain);
  }

  @Override
  public ScriptModule save(ScriptModule scriptModule) {
    ScriptModuleEntity entity = EntityMapper.toEntity(scriptModule);
    ScriptModuleEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public void deleteByName(String name) {
    jdbcRepository.findByName(name).ifPresent(entity -> jdbcRepository.deleteById(entity.getId()));
  }
}