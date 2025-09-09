package org.perun.registrarprototype.exceptions;

import org.perun.registrarprototype.models.FormItem;

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
