package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.persistance.FormItemRepository;
import org.perun.registrarprototype.persistance.FormRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.FormSpecificationEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormRepositoryJdbc implements FormRepository {

  private final FormSpecificationJdbcRepository jdbcRepository;
  private final FormItemRepository formItemRepository;

  public FormRepositoryJdbc(
      FormSpecificationJdbcRepository jdbcRepository,
      FormItemRepository formItemRepository) {
    this.jdbcRepository = jdbcRepository;
    this.formItemRepository = formItemRepository;
  }

  @Override
  public Optional<FormSpecification> findById(int formId) {
    return jdbcRepository.findById(formId)
        .map(entity -> {
          List<FormItem> items = formItemRepository.getFormItemsByFormId(formId);
          return toDomain(entity, items);
        });
  }

  @Override
  public Optional<FormSpecification> findByGroupId(String groupId) {
    return jdbcRepository.findByGroupId(groupId)
        .map(entity -> {
          List<FormItem> items = formItemRepository.getFormItemsByFormId(entity.getId());
          return toDomain(entity, items);
        });
  }

  @Override
  public FormSpecification save(FormSpecification formSpecification) {
    FormSpecificationEntity entity = toEntity(formSpecification);

    FormSpecificationEntity saved = jdbcRepository.save(entity);

    if (formSpecification.getItems() != null) {
      for (FormItem item : formSpecification.getItems()) {
        item.setFormId(saved.getId());
        formItemRepository.save(item);
      }
    }

    List<FormItem> items = formItemRepository.getFormItemsByFormId(saved.getId());
    return toDomain(saved, items);
  }

  @Override
  public FormSpecification update(FormSpecification formSpecification) {
    return save(formSpecification);
  }

  @Override
  public void delete(FormSpecification formSpecification) {
    jdbcRepository.deleteById(formSpecification.getId());
  }

  @Override
  public List<FormSpecification> findAll() {
    List<FormSpecification> result = new ArrayList<>();
    for (FormSpecificationEntity entity : jdbcRepository.findAll()) {
      List<FormItem> items = formItemRepository.getFormItemsByFormId(entity.getId());
      result.add(toDomain(entity, items));
    }
    return result;
  }

  private FormSpecification toDomain(FormSpecificationEntity entity, List<FormItem> items) {
    return new FormSpecification(
        entity.getId(),
        entity.getVoId(),
        entity.getGroupId(),
        items != null ? items : new ArrayList<>(),
        entity.getAutoApprove() != null ? entity.getAutoApprove() : false,
        entity.getAutoApproveExtension() != null ? entity.getAutoApproveExtension() : false
    );
  }

  private FormSpecificationEntity toEntity(FormSpecification form) {
    FormSpecificationEntity entity = new FormSpecificationEntity();
    if (form.getId() > 0) {
      entity.setId(form.getId());
    }
    entity.setVoId(form.getVoId());
    entity.setGroupId(form.getGroupId());
    entity.setAutoApprove(form.isAutoApprove());
    entity.setAutoApproveExtension(form.isAutoApproveExtension());
    return entity;
  }
}

