package org.perun.registrarprototype.services.modules;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.security.CurrentUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestModuleBeforeSubmission implements FormModule {

  @Override
  public String getName() {
    return "TestModuleBeforeSubmission";
  }

  @Override
  public String getName() {
    return "TestModuleBeforeSubmission";
  }

  @Override
  public void canBeSubmitted(CurrentUser sess, FormSpecification.FormType type, Map<String, String> options) {

  }

  @Override
  public void afterFormItemsPrefilled(CurrentUser sess, FormSpecification.FormType type, List<FormItemData> prefilledFormItems) {
    prefilledFormItems.getFirst().setPrefilledValue("testModuleValue");
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
