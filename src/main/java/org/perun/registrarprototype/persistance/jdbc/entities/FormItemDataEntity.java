package org.perun.registrarprototype.persistance.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_item_data")
public class FormItemDataEntity {
  @Id
  @Column("id")
  private Integer id;
  @Column("application_id")
  private Integer applicationId;
  @Column("form_item_id")
  private Integer formItemId;
  @Column("value")
  private String value;
  @Column("prefilled_value")
  private String prefilledValue;
  @Column("identity_attribute_value")
  private String identityAttributeValue;
  @Column("idm_attribute_value")
  private String idmAttributeValue;
  @Column("value_assured")
  private Boolean valueAssured;

  public FormItemDataEntity() {
  }

  public FormItemDataEntity(Integer id, Integer applicationId, Integer formItemId, String value, String prefilledValue,
                            String identityAttributeValue, String idmAttributeValue, Boolean valueAssured) {
    this.id = id;
    this.applicationId = applicationId;
    this.formItemId = formItemId;
    this.value = value;
    this.prefilledValue = prefilledValue;
    this.identityAttributeValue = identityAttributeValue;
    this.idmAttributeValue = idmAttributeValue;
    this.valueAssured = valueAssured;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  public Integer getFormItemId() {
    return formItemId;
  }

  public void setFormItemId(Integer formItemId) {
    this.formItemId = formItemId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getPrefilledValue() {
    return prefilledValue;
  }

  public void setPrefilledValue(String prefilledValue) {
    this.prefilledValue = prefilledValue;
  }

  public String getIdentityAttributeValue() {
    return identityAttributeValue;
  }

  public void setIdentityAttributeValue(String identityAttributeValue) {
    this.identityAttributeValue = identityAttributeValue;
  }

  public String getIdmAttributeValue() {
    return idmAttributeValue;
  }

  public void setIdmAttributeValue(String idmAttributeValue) {
    this.idmAttributeValue = idmAttributeValue;
  }

  public Boolean getValueAssured() {
    return valueAssured;
  }

  public void setValueAssured(Boolean valueAssured) {
    this.valueAssured = valueAssured;
  }
}