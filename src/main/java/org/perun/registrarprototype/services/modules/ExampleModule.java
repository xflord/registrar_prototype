package org.perun.registrarprototype.services.modules;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExampleModule implements FormModule {

  @Autowired
  private ApplicationService applicationService;

  @Override
  public String getName() {
    System.out.println(applicationService.getAllApplications());
    return "ExampleModule";
  }

  @Override
  public void canBeSubmitted(CurrentUser sess, Form.FormType type, Map<String, String> options) {

  }

  @Override
  public void afterFormItemsPrefilled(CurrentUser sess, Form.FormType type, List<FormItemData> prefilledFormItems) {

  }

  @Override
  public void afterApplicationSubmitted(Application application) {

  }

  @Override
  public void beforeApproval(Application application) {

  }

  @Override
  public void onApproval(Application application) {

  }

  @Override
  public void onAutoApproval(Application application) {

  }

  @Override
  public void onRejection(Application application) {

  }

  @Override
  public List<String> getRequiredOptions() {
    return List.of();
  }
}
