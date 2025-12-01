package org.perun.registrarprototype.models;

import java.util.Objects;

public class Destination {
  private int id;
  private String urn;
  private Integer formSpecificationId; // ID of the FormSpecification this Destination belongs to
  private boolean global;

  public Destination() {}

  public Destination(int id, String urn, Integer formSpecificationId, boolean global) {
    if (global) {
      if (formSpecificationId != null) {
        throw new IllegalArgumentException("Form specification ID must be null for global destinations");
      }
    } else {
      if (formSpecificationId == null) {
        throw new IllegalArgumentException("Form specification ID must not be null");
      }
    }
    this.id = id;
    this.global = global;
    this.formSpecificationId = formSpecificationId;
    this.urn = urn;
  }

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
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

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Destination that = (Destination) o;
    return isGlobal() == that.isGlobal() && Objects.equals(getUrn(), that.getUrn()) &&
               Objects.equals(getFormSpecificationId(), that.getFormSpecificationId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUrn(), getFormSpecificationId(), isGlobal());
  }


  @Override
  public String toString() {
    return "Destination{" +
               "id=" + id +
               ", urn='" + urn + '\'' +
               ", formSpecificationId=" + formSpecificationId +
               ", global=" + global +
               '}';
  }
}
