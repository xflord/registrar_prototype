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

  private Integer formSpecificationId; // ID of the FormSpecification this PrefillStrategyEntry belongs to
  private boolean global;

  public PrefillStrategyEntry() {}

  public PrefillStrategyEntry(int id, PrefillStrategyType type, Map<String, String> options, String sourceAttribute,
                              Integer formSpecificationId, boolean global) {
    this.id = id;
    this.type = type;
    this.options = options;
    this.sourceAttribute = sourceAttribute;
    this.formSpecificationId = formSpecificationId;
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
               Objects.equals(formSpecificationId, that.formSpecificationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, options, sourceAttribute, formSpecificationId, global);
  }

  @Override
  public String toString() {
    return "PrefillStrategyEntry{" +
               "prefillStrategyType=" + type +
               ", options=" + options +
               ", sourceAttribute='" + sourceAttribute + '\'' +
               ", formSpecificationId=" + formSpecificationId +
               ", global=" + global +
               '}';
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
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
