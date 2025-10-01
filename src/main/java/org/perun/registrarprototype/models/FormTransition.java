package org.perun.registrarprototype.models;

import java.util.List;

public class FormTransition {
  private Form sourceForm;
  private Form targetForm;
  private List<Form.FormType> sourceFormTypes = List.of(Form.FormType.INITIAL, Form.FormType.EXTENSION);
  private List<Form.FormType> targetFormTypes = List.of(Form.FormType.INITIAL, Form.FormType.EXTENSION);
  private TransitionType type;

  public FormTransition() {
  }

  public FormTransition(Form sourceForm, Form targetForm, List<Form.FormType> sourceFormTypes,
                        List<Form.FormType> targetFormTypes, TransitionType type) {
    this.sourceForm = sourceForm;
    this.targetForm = targetForm;
    this.sourceFormTypes = sourceFormTypes;
    this.targetFormTypes = targetFormTypes;
    this.type = type;
  }

  public Form getSourceForm() {
    return sourceForm;
  }

  public void setSourceForm(Form sourceForm) {
    this.sourceForm = sourceForm;
  }

  public Form getTargetForm() {
    return targetForm;
  }

  public void setTargetForm(Form targetForm) {
    this.targetForm = targetForm;
  }

  public List<Form.FormType> getSourceFormTypes() {
    return sourceFormTypes;
  }

  public void setSourceFormTypes(List<Form.FormType> sourceFormTypes) {
    this.sourceFormTypes = sourceFormTypes;
  }

  public List<Form.FormType> getTargetFormTypes() {
    return targetFormTypes;
  }

  public void setTargetFormTypes(List<Form.FormType> targetFormTypes) {
    this.targetFormTypes = targetFormTypes;
  }

  public TransitionType getType() {
    return type;
  }

  public void setType(TransitionType type) {
    this.type = type;
  }

  public enum TransitionType {
    PREREQUISITE,
    REDIRECT,
    AUTO_SUBMIT
  }
}
