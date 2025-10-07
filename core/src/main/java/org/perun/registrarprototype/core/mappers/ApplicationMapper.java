package org.perun.registrarprototype.core.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.perun.registrarprototype.core.models.Application;
import org.perun.registrarprototype.extension.dto.ApplicationDto;

@Mapper(componentModel = "spring", uses = FormItemDataMapper.class)
public interface ApplicationMapper {
  @Mapping(target = "submissionId", source = "submission.id")
  ApplicationDto toDto(Application app);

  @InheritInverseConfiguration
  @Mapping(target = "submission", ignore = true) // <--- important, change and create submissionDTO if we wish to modify it in modules
  Application toEntity(ApplicationDto dto);

  @Mapping(target = "submission", ignore = true) // <--- same here
  void updateEntity(ApplicationDto dto, @MappingTarget Application entity);
}

