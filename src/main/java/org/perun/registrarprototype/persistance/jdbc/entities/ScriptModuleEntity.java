package org.perun.registrarprototype.persistance.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("script_module")
public class ScriptModuleEntity {
  @Id
  @Column("id")
  private Integer id;
  @Column("name")
  private String name;
  @Column("script")
  private String script;

  public ScriptModuleEntity() {
  }

  public ScriptModuleEntity(Integer id, String name, String script) {
    this.id = id;
    this.name = name;
    this.script = script;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
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