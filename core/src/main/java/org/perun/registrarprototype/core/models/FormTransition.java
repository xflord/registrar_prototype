package org.perun.registrarprototype.core.models;

import java.util.List;
import org.perun.registrarprototype.extension.dto.FormType;

public class FormTransition {
  private Form sourceForm;
  private Form targetForm;
  private List<FormType> sourceFormTypes = List.of(FormType.INITIAL, FormType.EXTENSION);
  private List<FormType> targetFormTypes = List.of(FormType.INITIAL, FormType.EXTENSION);
  private TransitionType type;

  public FormTransition() {
  }

  public FormTransition(Form sourceForm, Form targetForm, List<FormType> sourceFormTypes,
                        List<FormType> targetFormTypes, TransitionType type) {
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

  public List<FormType> getSourceFormTypes() {
    return sourceFormTypes;
  }

  public void setSourceFormTypes(List<FormType> sourceFormTypes) {
    this.sourceFormTypes = sourceFormTypes;
  }

  public List<FormType> getTargetFormTypes() {
    return targetFormTypes;
  }

  public void setTargetFormTypes(List<FormType> targetFormTypes) {
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
