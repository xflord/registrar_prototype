package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.perun.registrarprototype.models.ScriptModule;
import org.perun.registrarprototype.persistance.ScriptModuleRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.ScriptModuleEntity;
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
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ScriptModule> findByName(String name) {
    return jdbcRepository.findByName(name)
        .map(this::toDomain);
  }

  @Override
  public ScriptModule save(ScriptModule scriptModule) {
    ScriptModuleEntity entity = toEntity(scriptModule);
    ScriptModuleEntity saved = jdbcRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public void deleteByName(String name) {
    jdbcRepository.findByName(name).ifPresent(entity -> jdbcRepository.deleteById(entity.getId()));
  }

  private ScriptModule toDomain(ScriptModuleEntity entity) {
    return new ScriptModule(
        entity.getId(),
        entity.getName(),
        entity.getScript()
    );
  }

  private ScriptModuleEntity toEntity(ScriptModule domain) {
    ScriptModuleEntity entity = new ScriptModuleEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setName(domain.getName());
    entity.setScript(domain.getScript());
    return entity;
  }
}