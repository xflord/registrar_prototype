package org.perun.registrarprototype.models;

import java.util.Map;
import java.util.Objects;

public class PrefillStrategyEntry {
  private FormItem.PrefillStrategyType prefillStrategyType;
  private Map<String, String> options;

  public PrefillStrategyEntry(FormItem.PrefillStrategyType prefillStrategyType, Map<String, String> options) {
    this.prefillStrategyType = prefillStrategyType;
    this.options = options;
  }

  public FormItem.PrefillStrategyType getPrefillStrategyType() {
    return prefillStrategyType;
  }

  public void setPrefillStrategyType(FormItem.PrefillStrategyType prefillStrategyType) {
    this.prefillStrategyType = prefillStrategyType;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrefillStrategyEntry that = (PrefillStrategyEntry) o;
    return getPrefillStrategyType() == that.getPrefillStrategyType() &&
               Objects.equals(getOptions(), that.getOptions());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPrefillStrategyType(), getOptions());
  }
}
