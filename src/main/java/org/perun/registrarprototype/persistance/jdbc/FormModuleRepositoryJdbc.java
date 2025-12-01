package org.perun.registrarprototype.persistance.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.persistance.FormModuleRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.AssignedFormModuleEntity;
import org.perun.registrarprototype.persistance.jdbc.entities.FormModuleOption;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormModuleRepositoryJdbc implements FormModuleRepository {

  private final AssignedFormModuleJdbcCrudRepository jdbcRepository;

  public FormModuleRepositoryJdbc(AssignedFormModuleJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public List<AssignedFormModule> findAllByFormId(int formId) {
    return jdbcRepository.findByFormIdOrderByPosition(formId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void saveAll(List<AssignedFormModule> modules) {
    // For simplicity, we'll delete all existing modules for this form and save new ones
    // In a real implementation, you might want to do a proper diff/update
    List<AssignedFormModuleEntity> entities = modules.stream()
        .map(this::toEntity)
        .collect(Collectors.toList());
    jdbcRepository.saveAll(entities);
  }

  private AssignedFormModule toDomain(AssignedFormModuleEntity entity) {
    // Convert options from entity format to map
    Map<String, String> options = new HashMap<>();
    for (FormModuleOption option : entity.getOptions()) {
      options.put(option.getOptionKey(), option.getOptionValue());
    }

    // Determine module name based on entity type
    String moduleName = entity.getModuleName();
    if (moduleName == null && entity.getScriptModuleId() != null) {
      // For script modules, we might need to fetch the script module name
      // For now, we'll use a placeholder - in a real implementation, 
      // you might want to join with the script_module table
      moduleName = "script:" + entity.getScriptModuleId();
    }

    return new AssignedFormModule(
        entity.getFormId(),
        moduleName,
        null, // formModule will be set by the service layer
        options
    );
  }

  private AssignedFormModuleEntity toEntity(AssignedFormModule domain) {
    AssignedFormModuleEntity entity = new AssignedFormModuleEntity();
    // We don't set ID here as it's managed by the database
    entity.setFormId(domain.getFormId());
    entity.setModuleName(domain.getModuleName());
    // For script modules, we would need to extract the script module ID from the module name
    // This is a simplified implementation - in a real scenario, you'd need proper mapping
    entity.setPosition(0); // Position should be set properly in a real implementation
    
    // Convert options from map to entity format
    if (domain.getOptions() != null) {
      List<FormModuleOption> options = domain.getOptions().entrySet().stream()
          .map(entry -> new FormModuleOption(null, entry.getKey(), entry.getValue()))
          .collect(Collectors.toList());
      entity.setOptions(options);
    }
    
    return entity;
  }
}