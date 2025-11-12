package org.perun.registrarprototype.controllers.dto;

import java.util.List;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Requirement;

public class FormTransitionDTO {
  private Integer id;
  private Integer sourceFormSpecificationId;
  private Integer targetFormSpecificationId;
  private List<Requirement.TargetState> sourceFormStates;
  private Requirement.TargetState targetFormState;
  private FormTransition.TransitionType type;

  public FormTransitionDTO() {}

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

  public List<Requirement.TargetState> getSourceFormStates() {
    return sourceFormStates;
  }

  public void setSourceFormStates(List<Requirement.TargetState> sourceFormStates) {
    this.sourceFormStates = sourceFormStates;
  }

  public Requirement.TargetState getTargetFormState() {
    return targetFormState;
  }

  public void setTargetFormState(Requirement.TargetState targetFormState) {
    this.targetFormState = targetFormState;
  }

  public FormTransition.TransitionType getType() {
    return type;
  }

  public void setType(FormTransition.TransitionType type) {
    this.type = type;
  }
}

