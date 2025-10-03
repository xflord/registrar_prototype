package org.perun.registrarprototype.core.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.perun.registrarprototype.core.models.Application;
import org.perun.registrarprototype.extension.dto.ApplicationDto;

@Mapper(componentModel = "spring", uses = FormItemDataMapper.class)
public interface ApplicationMapper {
  ApplicationDto toDto(Application app);

  @InheritInverseConfiguration
  Application toEntity(ApplicationDto dto);

  void updateEntity(ApplicationDto dto, @MappingTarget Application app);
}

