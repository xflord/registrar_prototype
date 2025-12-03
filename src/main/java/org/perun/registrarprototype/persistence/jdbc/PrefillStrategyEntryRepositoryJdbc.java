package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.persistence.PrefillStrategyEntryRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.PrefillStrategyEntryJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.PrefillStrategyEntryEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.PrefillStrategyOption;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class PrefillStrategyEntryRepositoryJdbc implements PrefillStrategyEntryRepository {

  private final PrefillStrategyEntryJdbcCrudRepository jdbcRepository;

  public PrefillStrategyEntryRepositoryJdbc(
      PrefillStrategyEntryJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public PrefillStrategyEntry save(PrefillStrategyEntry entry) {
    PrefillStrategyEntryEntity entity = EntityMapper.toEntity(entry);
    PrefillStrategyEntryEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public List<PrefillStrategyEntry> saveAll(List<PrefillStrategyEntry> entries) {
    List<PrefillStrategyEntryEntity> entities = entries.stream()
        .map(EntityMapper::toEntity)
        .collect(Collectors.toList());
    List<PrefillStrategyEntryEntity> saved = new ArrayList<>();
    jdbcRepository.saveAll(entities).forEach(saved::add);
    return saved.stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PrefillStrategyEntry> findById(int id) {
    return jdbcRepository.findById(id)
        .map(DomainMapper::toDomain);
  }

  @Override
  public List<PrefillStrategyEntry> findAllById(List<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }
    List<PrefillStrategyEntryEntity> entities = new ArrayList<>();
    jdbcRepository.findAllById(ids).forEach(entities::add);
    return entities.stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<PrefillStrategyEntry> findAllGlobal() {
    return jdbcRepository.findByGlobalTrue().stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<PrefillStrategyEntry> findByFormSpecification(FormSpecification formSpecification) {
    return jdbcRepository.findByFormSpecificationId(formSpecification.getId()).stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PrefillStrategyEntry> exists(PrefillStrategyEntry entry) {

    List<PrefillStrategyEntryEntity> found = jdbcRepository.findByFormSpecificationIdAndTypeAndSourceAttributeAndGlobal(entry.getFormSpecificationId(),
        entry.getType().toString(), entry.getSourceAttribute(), entry.isGlobal());
    if (found.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(DomainMapper.toDomain(found.getFirst()));
  }

  @Override
  public void delete(PrefillStrategyEntry entry) {
    jdbcRepository.deleteById(entry.getId());
  }

}

