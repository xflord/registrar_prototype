package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PrefillStrategyEntry {
  private PrefillStrategyType type;
  private Map<String, String> options;
  private String sourceAttribute;

  public PrefillStrategyEntry() {}

  public PrefillStrategyEntry(PrefillStrategyType type, Map<String, String> options, String sourceAttribute) {
    this.type = type;
    this.options = options;
    this.sourceAttribute = sourceAttribute;
  }

  public PrefillStrategyType getType() {
    return type;
  }

  public void setType(PrefillStrategyType type) {
    this.type = type;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  public String getSourceAttribute() {
    return sourceAttribute;
  }

  public void setSourceAttribute(String sourceAttribute) {
    this.sourceAttribute = sourceAttribute;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrefillStrategyEntry that = (PrefillStrategyEntry) o;
    return getType() == that.getType() &&
               getSourceAttribute().equals(that.getSourceAttribute()) &&
               Objects.equals(getOptions(), that.getOptions());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), getOptions(), getSourceAttribute());
  }

  @Override
  public String toString() {
    return "PrefillStrategyEntry{" +
               "prefillStrategyType=" + type +
               ", options=" + options +
               ", sourceAttribute='" + sourceAttribute + '\'' +
               '}';
  }

  public enum PrefillStrategyType {
    IDENTITY_ATTRIBUTE, IDM_ATTRIBUTE, LOGIN_ATTRIBUTE, APPLICATION;

    public List<String> getRequiredOptions() {
      return new ArrayList<>();
    }

    public boolean requiresSource() {
      return switch (this) {
        case IDENTITY_ATTRIBUTE, IDM_ATTRIBUTE -> true;
        default -> false;
      };
    }
  }
}
