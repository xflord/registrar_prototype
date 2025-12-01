package org.perun.registrarprototype.persistance.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("item_texts")
public class ItemTextsEntity {
  @Id
  @Column("id")
  private Integer id;

  @Column("item_definition_id")
  private Integer itemDefinitionId;

  @Column("locale")
  private String locale;

  @Column("label")
  private String label;

  @Column("help")
  private String help;

  @Column("error")
  private String error;

  public ItemTextsEntity() {
  }

  public ItemTextsEntity(Integer itemDefinitionId, String locale, String label, String help, String error) {
    this.itemDefinitionId = itemDefinitionId;
    this.locale = locale;
    this.label = label;
    this.help = help;
    this.error = error;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getItemDefinitionId() {
    return itemDefinitionId;
  }

  public void setItemDefinitionId(Integer itemDefinitionId) {
    this.itemDefinitionId = itemDefinitionId;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getHelp() {
    return help;
  }

  public void setHelp(String help) {
    this.help = help;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}

