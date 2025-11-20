package org.perun.registrarprototype.persistance.jdbc.entities;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("item_definition_prefill_strategies")
public class PrefillStrategyRef {
  private Integer itemDefinitionId;

  private Integer prefillStrategyEntryId;

  private Integer position;

  public PrefillStrategyRef() {
  }

  public PrefillStrategyRef(Integer itemDefinitionId, Integer prefillStrategyEntryId, Integer position) {
    this.itemDefinitionId = itemDefinitionId;
    this.prefillStrategyEntryId = prefillStrategyEntryId;
    this.position = position;
  }

  public Integer getItemDefinitionId() {
    return itemDefinitionId;
  }

  public void setItemDefinitionId(Integer itemDefinitionId) {
    this.itemDefinitionId = itemDefinitionId;
  }

  public Integer getPrefillStrategyEntryId() {
    return prefillStrategyEntryId;
  }

  public void setPrefillStrategyEntryId(Integer prefillStrategyEntryId) {
    this.prefillStrategyEntryId = prefillStrategyEntryId;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }
}

