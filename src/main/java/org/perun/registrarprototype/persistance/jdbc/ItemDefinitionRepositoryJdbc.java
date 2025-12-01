package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.persistance.ItemDefinitionRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.FormTypeRef;
import org.perun.registrarprototype.persistance.jdbc.entities.ItemDefinitionEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class ItemDefinitionRepositoryJdbc implements ItemDefinitionRepository {

  private final ItemDefinitionJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public ItemDefinitionRepositoryJdbc(
      ItemDefinitionJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public Optional<ItemDefinition> findById(int id) {
    return jdbcRepository.findById(id)
        .map(simpleDomainMapper::toDomain);
  }

  @Override
  public List<ItemDefinition> findAllById(List<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }
    List<ItemDefinitionEntity> entities = new ArrayList<>();
    jdbcRepository.findAllById(ids).forEach(entities::add);
    return entities.stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ItemDefinition> findByShortName(String shortName) {
    return jdbcRepository.findByDisplayName(shortName)
        .map(simpleDomainMapper::toDomain);
  }

  @Override
  public List<ItemDefinition> findAllGlobal() {
    return jdbcRepository.findByGlobalTrue().stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<ItemDefinition> findAllByForm(FormSpecification formSpecification) {
    return jdbcRepository.findByFormSpecificationId(formSpecification.getId()).stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public ItemDefinition save(ItemDefinition itemDefinition) {
    ItemDefinitionEntity entity = toEntity(itemDefinition);
    ItemDefinitionEntity saved = jdbcRepository.save(entity);
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public List<ItemDefinition> saveAll(List<ItemDefinition> itemDefinitions) {
    List<ItemDefinitionEntity> entities = itemDefinitions.stream()
        .map(this::toEntity)
        .collect(Collectors.toList());
    
    List<ItemDefinitionEntity> saved = new ArrayList<>();
    jdbcRepository.saveAll(entities).forEach(saved::add);
    
    return saved.stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(ItemDefinition itemDefinition) {
    jdbcRepository.deleteById(itemDefinition.getId());
  }

  private ItemDefinitionEntity toEntity(ItemDefinition domain) {
    ItemDefinitionEntity entity = new ItemDefinitionEntity();
    
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    
    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setDisplayName(domain.getDisplayName());
    entity.setType(domain.getType());
    entity.setUpdatable(domain.getUpdatable());
    entity.setRequired(domain.getRequired());
    entity.setValidator(domain.getValidator());
    entity.setDestinationId(domain.getDestinationId());
    entity.setHidden(domain.getHidden());
    entity.setDisabled(domain.getDisabled());
    entity.setDefaultValue(domain.getDefaultValue());
    entity.setGlobal(domain.isGlobal());
    entity.setFormTypes(domain.getFormTypes().stream().map(type -> new FormTypeRef(-1, type.toString())).collect(
        Collectors.toSet()));
    return entity;
  }
}
