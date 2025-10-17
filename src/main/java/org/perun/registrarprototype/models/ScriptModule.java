package org.perun.registrarprototype.models;

/**
 * Groovy script modules.
 * Make sure they cannot be deleted if they are assigned to a form.
 */
public class ScriptModule {
  private int id;
  private String name;
  private String script;

  public ScriptModule() {}

  public ScriptModule(int id, String name, String script) {
    this.id = id;
    this.name = name;
    this.script = script;
  }
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getScript() {
    return script;
  }
  public void setScript(String script) {
    this.script = script;
  }
}
