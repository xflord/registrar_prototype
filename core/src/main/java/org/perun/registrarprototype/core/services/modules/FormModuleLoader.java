package org.perun.registrarprototype.core.services.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.extension.services.modules.FormModule;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class FormModuleLoader {

  private final PluginManager pluginManager;
  private final ApplicationContext springContext;
  @Value("${modules.directory}")
  private String modulesDirectory = "/opt/RegistrarPrototype/plugins";
  private Map<String, FormModule> loadedModules = new HashMap<>();


  public FormModuleLoader(ApplicationContext springContext) {
    this.springContext = springContext;

    this.pluginManager = new DefaultPluginManager(Paths.get(modulesDirectory));

    pluginManager.loadPlugins();
    pluginManager.startPlugins();

    this.loadModules();
  }

  public List<FormModule> loadModules() {
    List<FormModule> modules = pluginManager.getExtensions(FormModule.class);
    AutowireCapableBeanFactory factory = springContext.getAutowireCapableBeanFactory();
    modules.forEach(module -> {
      factory.autowireBean(module);
      loadedModules.put(module.getName(), module);
      System.out.println("Loaded module: " + module.getName() + " (" + module.getClass().getName() + ")");
    });

    return new ArrayList<>(loadedModules.values());
  }

  public void loadModule(String moduleName) {
    String pluginId = pluginManager.loadPlugin(Paths.get(modulesDirectory, moduleName));
    pluginManager.startPlugin(pluginId);

    AutowireCapableBeanFactory factory = springContext.getAutowireCapableBeanFactory();
    // TODO make sure this only autowires the new module
    pluginManager.getExtensions(FormModule.class)
                 .forEach(module -> {
                   if (!loadedModules.containsKey(module.getName())) {
                     factory.autowireBean(module);
                     loadedModules.put(module.getName(), module);
                     System.out.println("Loaded module: " + module.getName() + " (" + module.getClass().getName() + ")");
                   }
                 });
  }

  public FormModule getModule(String name) {
    return loadedModules.get(name);
  }

  public List<String> availableModules() {
    return new ArrayList<>(loadedModules.keySet());
  }

}
