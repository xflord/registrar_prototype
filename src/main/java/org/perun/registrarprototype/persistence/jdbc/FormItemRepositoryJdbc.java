package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.persistence.FormItemRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.FormItemJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.FormItemEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormItemRepositoryJdbc implements FormItemRepository {

  private final FormItemJdbcCrudRepository jdbcRepository;

  public FormItemRepositoryJdbc(
      FormItemJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public List<FormItem> getFormItemsByFormId(int formId) {
    List<FormItemEntity> entities = jdbcRepository.findByFormId(formId);
    return DomainMapper.toDomainList(entities);
  }

  @Override
  public List<FormItem> getFormItemsByFormIdAndType(int formId, FormSpecification.FormType formType) {
    List<FormItemEntity> entities = jdbcRepository.findByFormSpecificationIdAndFormType(formId, formType);
    return DomainMapper.toDomainList(entities);
  }

  @Override
  public List<FormItem> getFormItemsByDestinationAttribute(String urn) {
    List<FormItemEntity> entities = jdbcRepository.findByDestinationUrn(urn);
    return DomainMapper.toDomainList(entities);
  }

  @Override
  public List<FormItem> getFormItemsByItemDefinitionId(Integer itemDefinitionId) {
    List<FormItemEntity> entities = jdbcRepository.findByItemDefinitionId(itemDefinitionId);
    return DomainMapper.toDomainList(entities);
  }

  @Override
  public Optional<FormItem> getFormItemById(int formItemId) {
    return jdbcRepository.findById(formItemId)
        .map(DomainMapper::toDomain);
  }

  @Override
  public FormItem save(FormItem formItem) {
    FormItemEntity entity = EntityMapper.toEntity(formItem);
    FormItemEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public List<FormItem> saveAll(List<FormItem> formItems) {
    List<FormItemEntity> entities = formItems.stream()
        .map(EntityMapper::toEntity)
        .collect(Collectors.toList());
    List<FormItemEntity> saved = new ArrayList<>();
    jdbcRepository.saveAll(entities).forEach(saved::add);
    return DomainMapper.toDomainList(saved);
  }

  @Override
  public FormItem update(FormItem formItem) {
    return save(formItem);
  }

  @Override
  public void delete(FormItem formItem) {
    jdbcRepository.deleteById(formItem.getId());
  }
}

