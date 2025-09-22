package org.perun.registrarprototype.services.modules;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.models.Application;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestModuleWithOptions implements FormModule {
  @Override
  public void beforeSubmission(Application application, Map<String, String> options) {

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
    return List.of("option1", "option2");
  }
}
