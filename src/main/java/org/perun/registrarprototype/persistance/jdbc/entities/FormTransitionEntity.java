package org.perun.registrarprototype.persistance.jdbc.entities;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Requirement;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_transition")
public class FormTransitionEntity {
  @Id
  private Integer id;

  private Integer sourceFormSpecificationId;

  private Integer targetFormSpecificationId;

  private Integer position;

  private String targetFormState;

  private String transitionType;

  @MappedCollection(idColumn = "form_transition_id")
  private List<SourceStateRef> sourceStates = new ArrayList<>();

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

  public List<SourceStateRef> getSourceStates() {
    return sourceStates;
  }

  public void setSourceStates(List<SourceStateRef> sourceStates) {
    this.sourceStates = sourceStates != null ? sourceStates : new ArrayList<>();
  }
}

