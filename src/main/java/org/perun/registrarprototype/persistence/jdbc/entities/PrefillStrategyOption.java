package org.perun.registrarprototype.persistence.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("prefill_strategy_options")
public class PrefillStrategyOption extends AuditEntity {
  @Id
  @Column("id")
  private Integer id;

  @Column("option_key")
  private String optionKey;

  @Column("option_value")
  private String optionValue;

  @Column("prefill_strategy_entry_id")
  private Integer prefillStrategyEntryId;

  public PrefillStrategyOption() {
  }

  public PrefillStrategyOption(String optionKey, String optionValue) {
    this.optionKey = optionKey;
    this.optionValue = optionValue;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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

  public Integer getPrefillStrategyEntryId() {
    return prefillStrategyEntryId;
  }

  public void setPrefillStrategyEntryId(Integer prefillStrategyEntryId) {
    this.prefillStrategyEntryId = prefillStrategyEntryId;
  }
}

