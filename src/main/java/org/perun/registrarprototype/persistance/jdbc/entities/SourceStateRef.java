package org.perun.registrarprototype.persistance.jdbc.entities;

import org.perun.registrarprototype.models.Requirement;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_transition_source_states")
public class SourceStateRef {
  private Integer formTransitionId;

  private String sourceState;

  public SourceStateRef() {
  }

  public SourceStateRef(Integer formTransitionId, String sourceState) {
    this.formTransitionId = formTransitionId;
    this.sourceState = sourceState;
  }

  public Integer getFormTransitionId() {
    return formTransitionId;
  }

  public void setFormTransitionId(Integer formTransitionId) {
    this.formTransitionId = formTransitionId;
  }

  public Requirement.TargetState getSourceState() {
    return sourceState != null ? Requirement.TargetState.valueOf(sourceState) : null;
  }

  public void setSourceState(Requirement.TargetState sourceState) {
    this.sourceState = sourceState != null ? sourceState.name() : null;
  }
}

