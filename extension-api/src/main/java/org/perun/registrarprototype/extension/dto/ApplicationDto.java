package org.perun.registrarprototype.extension.dto;

import java.util.List;

public class ApplicationDto {
  private int id;
  private int userId;
  private int formId;
  private List<FormItemDataDto> formItemData;
  private ApplicationState state = ApplicationState.PENDING;
  private FormType type;
  private int submissionId;
}
