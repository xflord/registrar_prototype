package org.perun.registrarprototype.models;

import java.util.Objects;

public class Destination {
  private String urn;
  private FormSpecification formSpecification;
  private boolean global;

  public Destination() {}

  public Destination(String urn, FormSpecification formSpecification, boolean global) {
    if (global) {
      if (formSpecification != null) {
        throw new IllegalArgumentException("Form specification must be null for global destinations");
      }
    } else {
      if (formSpecification == null) {
        throw new IllegalArgumentException("Form specification must not be null");
      }
    }

    this.global = global;
    this.formSpecification = formSpecification;
    this.urn = urn;
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
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

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Destination that = (Destination) o;
    return isGlobal() == that.isGlobal() && Objects.equals(getUrn(), that.getUrn()) &&
               Objects.equals(getFormSpecification(), that.getFormSpecification());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUrn(), getFormSpecification(), isGlobal());
  }

}
