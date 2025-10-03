package org.perun.registrarprototype.core.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.perun.registrarprototype.core.models.FormItem;
import org.perun.registrarprototype.extension.dto.FormItemDto;

@Mapper(componentModel = "spring")
public interface FormItemMapper {
  FormItemDto toDto(FormItem formItem);
  void updateEntity(FormItemDto dto, @MappingTarget FormItem formItem);
}
