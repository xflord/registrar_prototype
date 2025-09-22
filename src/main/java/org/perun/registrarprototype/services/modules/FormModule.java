package org.perun.registrarprototype.services.modules;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.models.Application;

/**
 * Interface for form modules, following the lifecycle of an application.
 */
public interface FormModule {

  /**
   * Called before an application is submitted, already containing prefilled data.
   * Use to check whether the application can be submitted or whether prefilled data is valid.
   * @param application
   * @param options sample options map, not sure whether this will end up being used
   */
  void beforeSubmission(Application application, Map<String, String> options);

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
