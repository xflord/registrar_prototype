package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.persistance.FormItemRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.FormItemEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormItemRepositoryJdbc implements FormItemRepository {

  private final FormItemJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public FormItemRepositoryJdbc(
      FormItemJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public List<FormItem> getFormItemsByFormId(int formId) {
    List<FormItemEntity> entities = jdbcRepository.findByFormId(formId);
    return simpleDomainMapper.toDomainList(entities);
  }

  @Override
  public List<FormItem> getFormItemsByDestinationAttribute(String urn) {
    List<FormItemEntity> entities = jdbcRepository.findByDestinationUrn(urn);
    return simpleDomainMapper.toDomainList(entities);
  }

  @Override
  public List<FormItem> getFormItemsByItemDefinitionId(Integer itemDefinitionId) {
    List<FormItemEntity> entities = jdbcRepository.findByItemDefinitionId(itemDefinitionId);
    return simpleDomainMapper.toDomainList(entities);
  }

  @Override
  public Optional<FormItem> getFormItemById(int formItemId) {
    return jdbcRepository.findById(formItemId)
        .map(simpleDomainMapper::toDomain);
  }

  @Override
  public FormItem save(FormItem formItem) {
    FormItemEntity entity = toEntity(formItem);
    FormItemEntity saved = jdbcRepository.save(entity);
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public List<FormItem> saveAll(List<FormItem> formItems) {
    List<FormItemEntity> entities = formItems.stream()
        .map(this::toEntity)
        .collect(Collectors.toList());
    List<FormItemEntity> saved = new ArrayList<>();
    jdbcRepository.saveAll(entities).forEach(saved::add);
    return simpleDomainMapper.toDomainList(saved);
  }

  @Override
  public FormItem update(FormItem formItem) {
    return save(formItem);
  }

  @Override
  public void delete(FormItem formItem) {
    jdbcRepository.deleteById(formItem.getId());
  }

  private FormItemEntity toEntity(FormItem domain) {
    FormItemEntity entity = new FormItemEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setFormId(domain.getFormSpecificationId());
    entity.setShortName(domain.getShortName());
    entity.setParentId(domain.getParentId());
    entity.setOrdNum(domain.getOrdNum());
    entity.setHiddenDependencyItemId(domain.getHiddenDependencyItemId());
    entity.setDisabledDependencyItemId(domain.getDisabledDependencyItemId());
    entity.setItemDefinitionId(domain.getItemDefinitionId());
    return entity;
  }
}

