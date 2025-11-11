package org.perun.registrarprototype.models;

import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PrefillStrategyEntry {
  private int id;

  private PrefillStrategyType type;
  private Map<String, String> options;
  private String sourceAttribute;

  private FormSpecification formSpecification;
  private boolean global;

  public PrefillStrategyEntry() {}

  public PrefillStrategyEntry(int id, PrefillStrategyType type, Map<String, String> options, String sourceAttribute,
                              FormSpecification formSpecification, boolean global) {
    this.id = id;
    this.type = type;
    this.options = options;
    this.sourceAttribute = sourceAttribute;
    this.formSpecification = formSpecification;
    this.global = global;
    this.checkPrefillStrategyOptions();
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
    return id == that.id && global == that.global && type == that.type &&
               Objects.equals(options, that.options) &&
               Objects.equals(sourceAttribute, that.sourceAttribute) &&
               Objects.equals(formSpecification, that.formSpecification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, options, sourceAttribute, formSpecification, global);
  }

  @Override
  public String toString() {
    return "PrefillStrategyEntry{" +
               "prefillStrategyType=" + type +
               ", options=" + options +
               ", sourceAttribute='" + sourceAttribute + '\'' +
               '}';
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public FormSpecification getFormSpecification() {
    return formSpecification;
  }

  public void setFormSpecification(FormSpecification formSpecification) {
    this.formSpecification = formSpecification;
  }

  public boolean isGlobal() {
    return global;
  }

  public void setGlobal(boolean global) {
    this.global = global;
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

  /**
   * Checks that the options map contains all the required entries
   */
  private void checkPrefillStrategyOptions() {
    if (this.getType().requiresSource() && StringUtils.isEmpty(this.getSourceAttribute())) {
      throw new IllegalArgumentException("Prefill strategy " + this.getType() + " requires attribute");
    }
    List<String> requiredOptions = this.getType().getRequiredOptions();
    if (!requiredOptions.isEmpty()) {
      if (this.getOptions() == null) {
        throw new IllegalArgumentException("No prefill options are defined but are required for strategy " + this);
      }
      if (!this.getOptions().keySet().containsAll(requiredOptions)) {
        throw new IllegalArgumentException("Missing required options ( " + requiredOptions + " for strategy " + this);
      }
    }
  }
}
