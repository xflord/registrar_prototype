package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.ScriptModule;

public interface ScriptModuleRepository {
  List<ScriptModule> findAll();
  Optional<ScriptModule> findByName(String name);
  ScriptModule save(ScriptModule scriptModule);
  void deleteByName(String name);
}
