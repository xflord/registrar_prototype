package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.persistance.PrefillStrategyEntryRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.PrefillStrategyEntryEntity;
import org.perun.registrarprototype.persistance.jdbc.entities.PrefillStrategyOption;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class PrefillStrategyEntryRepositoryJdbc implements PrefillStrategyEntryRepository {

  private final PrefillStrategyEntryJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public PrefillStrategyEntryRepositoryJdbc(
      PrefillStrategyEntryJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public PrefillStrategyEntry save(PrefillStrategyEntry entry) {
    PrefillStrategyEntryEntity entity = toEntity(entry);
    PrefillStrategyEntryEntity saved = jdbcRepository.save(entity);
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public List<PrefillStrategyEntry> saveAll(List<PrefillStrategyEntry> entries) {
    List<PrefillStrategyEntryEntity> entities = entries.stream()
        .map(this::toEntity)
        .collect(Collectors.toList());
    List<PrefillStrategyEntryEntity> saved = new ArrayList<>();
    jdbcRepository.saveAll(entities).forEach(saved::add);
    return saved.stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PrefillStrategyEntry> findById(int id) {
    return jdbcRepository.findById(id)
        .map(simpleDomainMapper::toDomain);
  }

  @Override
  public List<PrefillStrategyEntry> findAllById(List<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }
    List<PrefillStrategyEntryEntity> entities = new ArrayList<>();
    jdbcRepository.findAllById(ids).forEach(entities::add);
    return entities.stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<PrefillStrategyEntry> findAllGlobal() {
    return jdbcRepository.findByGlobalTrue().stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<PrefillStrategyEntry> findByFormSpecification(FormSpecification formSpecification) {
    return jdbcRepository.findByFormSpecificationId(formSpecification.getId()).stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PrefillStrategyEntry> exists(PrefillStrategyEntry entry) {

    List<PrefillStrategyEntryEntity> found = jdbcRepository.findByFormSpecificationIdAndTypeAndSourceAttributeAndGlobal(entry.getFormSpecificationId(),
        entry.getType().toString(), entry.getSourceAttribute(), entry.isGlobal());
    if (found.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(simpleDomainMapper.toDomain(found.getFirst()));
  }

  @Override
  public void delete(PrefillStrategyEntry entry) {
    jdbcRepository.deleteById(entry.getId());
  }

  private PrefillStrategyEntryEntity toEntity(PrefillStrategyEntry domain) {
    PrefillStrategyEntryEntity entity = new PrefillStrategyEntryEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setType(domain.getType());
    entity.setSourceAttribute(domain.getSourceAttribute());
    entity.setGlobal(domain.isGlobal());
    
    List<PrefillStrategyOption> options = new ArrayList<>();
    if (domain.getOptions() != null) {
      for (Map.Entry<String, String> entry : domain.getOptions().entrySet()) {
        PrefillStrategyOption option = new PrefillStrategyOption();
        option.setOptionKey(entry.getKey());
        option.setOptionValue(entry.getValue());
        options.add(option);
      }
    }
    entity.setOptions(options);
    
    return entity;
  }
}

