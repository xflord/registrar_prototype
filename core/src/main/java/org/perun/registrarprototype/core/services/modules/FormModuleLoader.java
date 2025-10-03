package org.perun.registrarprototype.core.services.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.extension.services.modules.FormModule;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginManager;
import org.pf4j.RuntimeMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class FormModuleLoader {

  private final PluginManager pluginManager;
  private final ApplicationContext springContext;
  @Value("${modules.directory}")
  private String modulesDirectory;
  private Map<String, FormModule> loadedModules = new HashMap<>();


  public FormModuleLoader(ApplicationContext springContext) {
    this.springContext = springContext;
//    this.pluginManager = new DefaultPluginManager(Paths.get(modulesDirectory));
    // create the plugin manager
     this.pluginManager = new DefaultPluginManager() {
            @Override
            public RuntimeMode getRuntimeMode() {
                // Force PF4J to also discover system extensions on the classpath
                return RuntimeMode.DEVELOPMENT;
            }
        };

    pluginManager.loadPlugins();
    pluginManager.startPlugins();
    pluginManager.getExtensions(FormModule.class)
    .forEach(m -> System.out.println("Loaded module: " + m.getName() + " (" + m.getClass().getName() + ")"));

    this.loadModules();
    System.out.println("CONSTRUCTED LOADER");
  }

  public List<FormModule> loadModules() {
    List<FormModule> modules = pluginManager.getExtensions(FormModule.class);
    AutowireCapableBeanFactory factory = springContext.getAutowireCapableBeanFactory();
    modules.forEach(module -> {
      factory.autowireBean(module);
      loadedModules.put(module.getName(), module);
    });

    System.out.println(modules);

    return new ArrayList<>(loadedModules.values());
  }

  public void loadModule(Path jarPath) {
    String pluginId = pluginManager.loadPlugin(jarPath);
    pluginManager.startPlugin(pluginId);

    AutowireCapableBeanFactory factory = springContext.getAutowireCapableBeanFactory();
    // TODO make sure this only autowires the new module
    pluginManager.getExtensions(FormModule.class)
                 .forEach(module -> {
                   factory.autowireBean(module);
                   loadedModules.putIfAbsent(module.getName(), module);
                 });
  }

  public FormModule getModule(String name) {
    System.out.println(loadedModules.keySet());
    return loadedModules.get(name);
  }

  public List<String> availableModules() {
    return new ArrayList<>(loadedModules.keySet());
  }

}
