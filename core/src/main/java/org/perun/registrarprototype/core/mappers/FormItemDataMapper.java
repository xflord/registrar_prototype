package org.perun.registrarprototype.core.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.perun.registrarprototype.core.models.Application;
import org.perun.registrarprototype.core.models.FormItemData;
import org.perun.registrarprototype.extension.dto.FormItemDataDto;

@Mapper(componentModel = "spring", uses = FormItemMapper.class)
public interface FormItemDataMapper {
  FormItemDataDto toDto(FormItemData formItemData);
  void updateEntity(FormItemDataDto dto, @MappingTarget FormItemData formItemData);
}
