package org.perun.registrarprototype.extension.dto;

import java.util.List;

public class FormItemDto {
  private int id;
  private int formId;
  private ItemType type;
  //texts
  private boolean required;
  private String constraint; // regex or similar
  private String sourceIdentityAttribute;
  private String sourceIdmAttribute;
  private String destinationIdmAttribute;
  private boolean preferIdentityAttribute; // use IdM value if false, oauth claim value if true (and available)
  private String defaultValue;
  private List<FormType> formTypes = List.of(FormType.INITIAL, FormType.EXTENSION);
  private ItemCondition hidden;
  private ItemCondition disabled;
  private Integer hiddenDependencyItemId;
  private Integer disabledDependencyItemId;

}
