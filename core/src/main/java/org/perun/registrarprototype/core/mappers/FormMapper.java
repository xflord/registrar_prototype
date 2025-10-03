package org.perun.registrarprototype.core.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.extension.dto.FormDto;

@Mapper(componentModel = "spring", uses = FormItemMapper.class)
public interface FormMapper {
  FormDto toDto(Form form);
  void updateEntity(FormDto dto, @MappingTarget Form form);
}
