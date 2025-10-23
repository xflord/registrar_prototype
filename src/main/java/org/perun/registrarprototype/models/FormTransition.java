package org.perun.registrarprototype.models;

import java.util.List;

public class FormTransition {
  private int id = 0;
  private FormSpecification sourceFormSpecification;
  private FormSpecification targetFormSpecification;
//  private List<FormSpecification.FormType> sourceFormTypes = List.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION);
//  private List<FormSpecification.FormType> targetFormTypes = List.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION);
  private List<Requirement.TargetState> sourceFormStates;
  private Requirement.TargetState targetFormState;
  private TransitionType type;

  public FormTransition() {
  }

  public FormTransition(FormSpecification sourceFormSpecification, FormSpecification targetFormSpecification,
                        List<Requirement.TargetState> sourceFormStates, Requirement.TargetState targetFormState,
                        TransitionType type) {
    this.sourceFormSpecification = sourceFormSpecification;
    this.targetFormSpecification = targetFormSpecification;
    this.sourceFormStates = sourceFormStates;
    this.targetFormState = targetFormState;
    this.type = type;
  }

  public FormSpecification getSourceForm() {
    return sourceFormSpecification;
  }

  public void setSourceForm(FormSpecification sourceFormSpecification) {
    this.sourceFormSpecification = sourceFormSpecification;
  }

  public FormSpecification getTargetForm() {
    return targetFormSpecification;
  }

  public void setTargetForm(FormSpecification targetFormSpecification) {
    this.targetFormSpecification = targetFormSpecification;
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

  public TransitionType getType() {
    return type;
  }

  public void setType(TransitionType type) {
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public enum TransitionType {
    PREREQUISITE,
    REDIRECT,
    AUTO_SUBMIT
  }
}
