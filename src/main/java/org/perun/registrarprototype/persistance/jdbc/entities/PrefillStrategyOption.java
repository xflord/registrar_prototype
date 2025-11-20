package org.perun.registrarprototype.persistance.jdbc.entities;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("prefill_strategy_options")
public class PrefillStrategyOption {
  private Integer prefillStrategyEntryId;

  private String optionKey;

  private String optionValue;

  public PrefillStrategyOption() {
  }

  public PrefillStrategyOption(Integer prefillStrategyEntryId, String optionKey, String optionValue) {
    this.prefillStrategyEntryId = prefillStrategyEntryId;
    this.optionKey = optionKey;
    this.optionValue = optionValue;
  }

  public Integer getPrefillStrategyEntryId() {
    return prefillStrategyEntryId;
  }

  public void setPrefillStrategyEntryId(Integer prefillStrategyEntryId) {
    this.prefillStrategyEntryId = prefillStrategyEntryId;
  }

  public String getOptionKey() {
    return optionKey;
  }

  public void setOptionKey(String optionKey) {
    this.optionKey = optionKey;
  }

  public String getOptionValue() {
    return optionValue;
  }

  public void setOptionValue(String optionValue) {
    this.optionValue = optionValue;
  }
}

