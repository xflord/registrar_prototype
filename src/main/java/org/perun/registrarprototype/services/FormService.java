package org.perun.registrarprototype.services;

import java.util.List;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;

/**
 * Service for managing forms, form items, form modules, and form transitions.
 */
public interface FormService {

  /**
   * Creates a new form for the specified group with an empty list of items.
   *
   * @param groupId ID of the group for which to create the form
   * @return the created form
   * @throws FormItemRegexNotValid if any form item regex is invalid
   * @throws InsufficientRightsException if the user is not authorized to create a form
   */
  Form createForm(int groupId) throws FormItemRegexNotValid, InsufficientRightsException;

  /**
   * Creates a new form for the group and other properties specified in the form object and assigns the specified modules.
   * @param form form object with group id, name, description, etc., id will be assigned
   * @param moduleNames module objects with the name and options set, form id will be overwritten
   * @return created form
   */
  Form createForm(Form form, List<AssignedFormModule> moduleNames) throws InsufficientRightsException;

  /**
   * Creates a new form for the specified group with the provided list of items.
   *
   * @param groupId ID of the group for which to create the form
   * @param items list of form items to add to the form
   * @return the created form with items
   * @throws FormItemRegexNotValid if any form item regex is invalid
   * @throws InsufficientRightsException if the user is not authorized to create a form
   */
  Form createForm(int groupId, List<FormItem> items) throws FormItemRegexNotValid, InsufficientRightsException;

  /**
   * Adds or updates a form item for the specified form. Validates the item's regex constraint if present.
   *
   * @param formId ID of the form to add the item to
   * @param formItem the form item to add or update
   * @return the saved form item
   * @throws FormItemRegexNotValid if the form item's regex constraint is invalid
   */
  FormItem setFormItem(int formId, FormItem formItem) throws FormItemRegexNotValid;

  /**
   * Sets multiple form items for the specified form. Validates all regex constraints before saving.
   *
   * @param formId ID of the form to set items for
   * @param items list of form items to set
   * @throws FormItemRegexNotValid if any form item's regex constraint is invalid
   */
  void setFormItems(int formId, List<FormItem> items) throws FormItemRegexNotValid;

  /**
   * Retrieves all assigned form modules for a form, including their module components.
   *
   * @param form the form to retrieve modules for
   * @return list of assigned form modules with components set
   */
  List<AssignedFormModule> getAssignedFormModules(Form form);

  /**
   * Sets modules for the form. Checks whether the module actually exists and whether all the required options are set.
   *
   * @param sess authenticated session token
   * @param formId ID of the form to assign modules to
   * @param modulesToAssign modules with module name and options set (the rest is ignored)
   * @return list of assigned form modules
   * @throws InsufficientRightsException if the user is not authorized to manage the form
   */
  List<AssignedFormModule> setModules(RegistrarAuthenticationToken sess, int formId,
                                      List<AssignedFormModule> modulesToAssign) throws InsufficientRightsException;

  /**
   * Retrieves all forms with their items populated.
   *
   * @return list of all forms with items
   */
  List<Form> getAllFormsWithItems();

  /**
   * Retrieves a form by its ID.
   *
   * @param formId ID of the form to retrieve
   * @return the form
   */
  Form getFormById(int formId);

  /**
   * Retrieves all forms that are required to be filled before applying for membership via the supplied form.
   *
   * @param form the source form
   * @param type the form type to filter by
   * @return list of prerequisite form transitions
   */
  List<FormTransition> getPrerequisiteTransitions(Form form, Form.FormType type);

  /**
   * Retrieves all forms that are automatically submitted after the supplied form is submitted (AKA embedded).
   *
   * @param form the source form
   * @param type the form type to filter by
   * @return list of forms to auto-submit
   */
  List<Form> getAutosubmitForms(Form form, Form.FormType type);

  /**
   * Retrieves all forms that user is redirected to after submitting the supplied form.
   *
   * @param form the source form
   * @param type the form type to filter by
   * @return list of forms to redirect to
   */
  List<Form> getRedirectForms(Form form, Form.FormType type);

  /**
   * Retrieves form items for a specific form and form type.
   *
   * @param form the form to retrieve items for
   * @param type the form type to filter items by
   * @return list of form items for the specified type
   */
  List<FormItem> getFormItems(Form form, Form.FormType type);

  /**
   * Retrieves a form item by its ID.
   *
   * @param formItemId ID of the form item to retrieve
   * @return the form item
   */
  FormItem getFormItemById(int formItemId);

  /**
   * Creates a new form item.
   *
   * @param item the form item to create
   * @return the created form item
   */
  FormItem createFormItem(FormItem item);

  void updateFormItems( int formId, List<FormItem> newItems);
}

