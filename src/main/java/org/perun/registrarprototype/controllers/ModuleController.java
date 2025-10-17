package org.perun.registrarprototype.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.perun.registrarprototype.models.ScriptModule;
import org.perun.registrarprototype.services.modules.ModulesManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/modules")
public class ModuleController {
  private final ModulesManager modulesManager;

  public ModuleController(ModulesManager modulesManager) {
    this.modulesManager = modulesManager;
  }

  @GetMapping
  public ResponseEntity<Set<String>> getLoadedModulesNames() {
    return ResponseEntity.ok(modulesManager.getLoadedModules().keySet());
  }

  @GetMapping("/test")
  public ResponseEntity<List<String>> getTestModulesNames() {
    List<String> names = new ArrayList<>();

    for (String name : modulesManager.getLoadedModules().keySet()) {
      names.add(modulesManager.getModule(name).getName());
    }
    return ResponseEntity.ok(names);
  }

  /**
   * Upload a new module. Be careful to espace the json newlines when uploading script via api
   * @param module
   * @return
   */
  @PostMapping("/upload")
  public ResponseEntity<?> uploadModule(@RequestBody ScriptModule module) {
    try {
      modulesManager.uploadOrUpdate(module.getName(), module.getScript());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

}
