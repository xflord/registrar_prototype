package org.perun.registrarprototype.services.modules;


import groovy.lang.GroovyClassLoader;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.perun.registrarprototype.models.ScriptModule;
import org.perun.registrarprototype.repositories.ScriptModuleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Manager for loading modules, be it hardcoded ones, or groovy script ones (handles their uploading, persistence, reloading).
 * Also includes some concurrency control in case of concurrent module upload/reload.
 * Make sure to discuss potential security issues with the team before MVP
 */
@Service
public class ModulesManager {
  private final ScriptModuleRepository scriptModuleRepository;
  private final ApplicationContext context;
  private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

  private final Map<String, FormModule> loadedModules = new ConcurrentHashMap<>();

  public ModulesManager(ApplicationContext context, ScriptModuleRepository scriptModuleRepository) {
    this.context = context;
    this.scriptModuleRepository = scriptModuleRepository;
  }

  // once all beans are loaded, load all modules
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    this.context.getBeansOfType(FormModule.class).values()
        .forEach((module) -> loadedModules.put(module.getName(), module));

    this.scriptModuleRepository.findAll().forEach((module) -> safeLoad(module.getName(), module.getScript()));
    System.out.println("Loaded modules: " + loadedModules.keySet());
  }

  public synchronized void safeLoad(String name, String script) {
    try {
      Class<?> clazz = groovyClassLoader.parseClass(script, name.concat(".groovy"));
      Object instance = clazz.getDeclaredConstructor().newInstance();
      context.getAutowireCapableBeanFactory().autowireBean(instance);

      if (instance instanceof FormModule module) {
        System.out.println("Loaded module " + module.getName());
        loadedModules.put(module.getName(), module);
        System.out.println(loadedModules.keySet());
      } else {
        throw new IllegalArgumentException("Script " + name + " does not implement FormModule interface.");
      }
    } catch (MultipleCompilationErrorsException e) {
      // TODO add more sophisticated handling of compilation errors to display in the GUI editor
      throw new IllegalArgumentException("Script " + name + " has compilation errors: " + e.getMessage());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load script " + name + " with error: " + e.getMessage());
    }
  }

  public synchronized void reloadAll() {
    this.loadedModules.clear();
    this.init();
  }

  public synchronized void uploadOrUpdate(String name, String script) {
    scriptModuleRepository.findByName(name).ifPresentOrElse(scriptModule -> {
      scriptModule.setScript(script);
      scriptModuleRepository.save(scriptModule);
    }, () -> {
      ScriptModule newModule = new ScriptModule(0, name, script);
      scriptModuleRepository.save(newModule);
    });
    this.safeLoad(name, script);
  }

  public Map<String, FormModule> getLoadedModules() {
    return new HashMap<>(loadedModules);
  }

  public FormModule getModule(String name) {
    return loadedModules.get(name);
  }

  public void delete(String name) {
    scriptModuleRepository.findByName(name).ifPresent(scriptModule -> {
      scriptModuleRepository.deleteByName(name);
      loadedModules.remove(name);
    });
  }
}