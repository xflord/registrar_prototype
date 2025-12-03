package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.persistence.ItemDefinitionRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.ItemDefinitionJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.FormTypeRef;
import org.perun.registrarprototype.persistence.jdbc.entities.ItemDefinitionEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class ItemDefinitionRepositoryJdbc implements ItemDefinitionRepository {

  private final ItemDefinitionJdbcCrudRepository jdbcRepository;

  public ItemDefinitionRepositoryJdbc(ItemDefinitionJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public Optional<ItemDefinition> findById(int id) {
    return jdbcRepository.findById(id)
        .map(DomainMapper::toDomain);
  }

  @Override
  public List<ItemDefinition> findAllById(List<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }
    List<ItemDefinitionEntity> entities = new ArrayList<>();
    jdbcRepository.findAllById(ids).forEach(entities::add);
    return entities.stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ItemDefinition> findByShortName(String shortName) {
    return jdbcRepository.findByDisplayName(shortName)
        .map(DomainMapper::toDomain);
  }

  @Override
  public List<ItemDefinition> findAllGlobal() {
    return jdbcRepository.findByGlobalTrue().stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<ItemDefinition> findAllByForm(FormSpecification formSpecification) {
    return jdbcRepository.findByFormSpecificationId(formSpecification.getId()).stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public ItemDefinition save(ItemDefinition itemDefinition) {
    ItemDefinitionEntity entity = EntityMapper.toEntity(itemDefinition);
    ItemDefinitionEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public List<ItemDefinition> saveAll(List<ItemDefinition> itemDefinitions) {
    List<ItemDefinitionEntity> entities = itemDefinitions.stream()
        .map(EntityMapper::toEntity)
        .collect(Collectors.toList());
    
    List<ItemDefinitionEntity> saved = new ArrayList<>();
    jdbcRepository.saveAll(entities).forEach(saved::add);
    
    return saved.stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(ItemDefinition itemDefinition) {
    jdbcRepository.deleteById(itemDefinition.getId());
  }
}
