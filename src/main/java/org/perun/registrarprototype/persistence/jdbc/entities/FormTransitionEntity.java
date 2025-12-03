package org.perun.registrarprototype.persistence.jdbc.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Requirement;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_transition")
public class FormTransitionEntity {
  @Id
  @Column("id")
  private Integer id;

  @Column("source_form_specification_id")
  private Integer sourceFormSpecificationId;

  @Column("target_form_specification_id")
  private Integer targetFormSpecificationId;

  @Column("position")
  private Integer position;

  @Column("target_form_state")
  private String targetFormState;

  @Column("transition_type")
  private String transitionType;

  @MappedCollection(idColumn = "form_transition_id")
  private Set<SourceStateRef> sourceStates;

  public FormTransitionEntity() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getSourceFormSpecificationId() {
    return sourceFormSpecificationId;
  }

  public void setSourceFormSpecificationId(Integer sourceFormSpecificationId) {
    this.sourceFormSpecificationId = sourceFormSpecificationId;
  }

  public Integer getTargetFormSpecificationId() {
    return targetFormSpecificationId;
  }

  public void setTargetFormSpecificationId(Integer targetFormSpecificationId) {
    this.targetFormSpecificationId = targetFormSpecificationId;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  public Requirement.TargetState getTargetFormState() {
    return targetFormState != null ? Requirement.TargetState.valueOf(targetFormState) : null;
  }

  public void setTargetFormState(Requirement.TargetState targetFormState) {
    this.targetFormState = targetFormState != null ? targetFormState.name() : null;
  }

  public FormTransition.TransitionType getTransitionType() {
    return transitionType != null ? FormTransition.TransitionType.valueOf(transitionType) : null;
  }

  public void setTransitionType(FormTransition.TransitionType transitionType) {
    this.transitionType = transitionType != null ? transitionType.name() : null;
  }

  public Set<SourceStateRef> getSourceStates() {
    return sourceStates;
  }

  public void setSourceStates(List<SourceStateRef> sourceStates) {
    this.sourceStates = sourceStates != null ? new HashSet<>(sourceStates) : new HashSet<>();
  }
}

