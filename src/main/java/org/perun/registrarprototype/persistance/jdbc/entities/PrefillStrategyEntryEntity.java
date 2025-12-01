package org.perun.registrarprototype.persistance.jdbc.entities;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("prefill_strategy_entry")
public class PrefillStrategyEntryEntity {
  @Id
  @Column("id")
  private Integer id;

  @Column("form_specification_id")
  private Integer formSpecificationId;

  @Column("type")
  private String type;

  @Column("source_attribute")
  private String sourceAttribute;

  @Column("global")
  private Boolean global;

  @MappedCollection(idColumn = "prefill_strategy_entry_id", keyColumn = "id")
  private List<PrefillStrategyOption> options = new ArrayList<>();

  public PrefillStrategyEntryEntity() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  public PrefillStrategyEntry.PrefillStrategyType getType() {
    return type != null ? PrefillStrategyEntry.PrefillStrategyType.valueOf(type) : null;
  }

  public void setType(PrefillStrategyEntry.PrefillStrategyType type) {
    this.type = type != null ? type.name() : null;
  }

  public String getSourceAttribute() {
    return sourceAttribute;
  }

  public void setSourceAttribute(String sourceAttribute) {
    this.sourceAttribute = sourceAttribute;
  }

  public Boolean getGlobal() {
    return global;
  }

  public void setGlobal(Boolean global) {
    this.global = global;
  }

  public List<PrefillStrategyOption> getOptions() {
    return options;
  }

  public void setOptions(List<PrefillStrategyOption> options) {
    this.options = options != null ? options : new ArrayList<>();
  }
}

