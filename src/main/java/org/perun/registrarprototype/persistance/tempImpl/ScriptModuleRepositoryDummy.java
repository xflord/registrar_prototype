package org.perun.registrarprototype.persistance.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.ScriptModule;
import org.perun.registrarprototype.persistance.ScriptModuleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!jdbc")
@Component
public class ScriptModuleRepositoryDummy implements ScriptModuleRepository {
  public static final List<ScriptModule> modules = new ArrayList<>();
  private static int currId = 1;

  @Override
  public List<ScriptModule> findAll() {
    return List.copyOf(modules);
  }

  @Override
  public Optional<ScriptModule> findByName(String name) {
    return modules.stream()
        .filter(module -> module.getName().equals(name))
        .findFirst();
  }

  @Override
  public ScriptModule save(ScriptModule scriptModule) {
    Optional<ScriptModule> existingModule = findByName(scriptModule.getName());
    if (existingModule.isPresent()) {
      existingModule.get().setScript(scriptModule.getScript());
      return existingModule.get();
    }
    scriptModule.setId(currId++);
    modules.add(scriptModule);
    return scriptModule;
  }

  @Override
  public void deleteByName(String name) {
    modules.removeIf(module -> module.getName().equals(name));
  }
}
