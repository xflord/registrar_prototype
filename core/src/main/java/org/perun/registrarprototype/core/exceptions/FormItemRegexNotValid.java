package org.perun.registrarprototype.core.exceptions;

import org.perun.registrarprototype.core.models.FormItem;

public class FormItemRegexNotValid extends Exception {
  private FormItem formItem;

  public FormItemRegexNotValid(String message) {
    super(message);
  }
  public FormItemRegexNotValid(String message, FormItem formItem) {
    this(message);
    this.formItem = formItem;
  }

  public FormItem getFormItem() {
    return formItem;
  }
}
