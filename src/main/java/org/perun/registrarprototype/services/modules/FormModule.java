package org.perun.registrarprototype.services.modules;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.security.CurrentUser;

/**
 * Interface for form modules, following the lifecycle of an application.
 */
public interface FormModule {

  /**
   * Called before an application is submitted, already containing prefilled data.
   * Use to check whether the application can be submitted or whether prefilled data is valid.
   * @param sess
   * @param options sample options map, not sure whether this will end up being used
   */
  void canBeSubmitted(CurrentUser sess, FormSpecification.FormType type, Map<String, String> options);

  void afterFormItemsPrefilled(CurrentUser sess, FormSpecification.FormType type, List<FormItemData> prefilledFormItems);

  /**
   * Called after an application is submitted.
   * Use to add custom logic to run after submission - like sending an email, formatting email/phone numbers, etc.
   * TODO consider this not being a module but hardcoded business logic.
   * @param application
   */
  void afterApplicationSubmitted(Application application);

  /**
   * Called before an application is approved.
   * Use to check whether the application can be approved or add custom logic to run before approval.
   * @param application
   */
  void beforeApproval(Application application);

  /**
   * Called after an application is approved.
   * Use to add custom logic to run after approval.
   * @param application
   */
  void onApproval(Application application);

  /**
   * Called after an application is auto-approved.
   * Use to add custom logic to run specifically after auto-approval.
   * @param application
   */
  void onAutoApproval(Application application);

  /**
   * Called after an application is rejected.
   * Use to add custom logic to run after rejection.
   * @param application
   */
  void onRejection(Application application);

  /**
   * Get a list of options that are required for the module to function properly.
   * Ideally use this so that we can reuse the module for multiple cases.
   * @return
   */
  List<String> getRequiredOptions();
}
