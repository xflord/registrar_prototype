package org.perun.registrarprototype.controllers.dto;

import java.util.Map;
import org.perun.registrarprototype.models.PrefillStrategyEntry;

public class PrefillStrategyEntryDTO {
  private Integer id;
  private PrefillStrategyEntry.PrefillStrategyType type;
  private Map<String, String> options;
  private String sourceAttribute;
  private Integer formSpecificationId;
  private boolean global;

  public PrefillStrategyEntryDTO() {}

  public PrefillStrategyEntryDTO(Integer id, PrefillStrategyEntry.PrefillStrategyType type, 
                                 Map<String, String> options, String sourceAttribute,
                                 Integer formSpecificationId, boolean global) {
    this.id = id;
    this.type = type;
    this.options = options;
    this.sourceAttribute = sourceAttribute;
    this.formSpecificationId = formSpecificationId;
    this.global = global;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PrefillStrategyEntry.PrefillStrategyType getType() {
    return type;
  }

  public void setType(PrefillStrategyEntry.PrefillStrategyType type) {
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
}

