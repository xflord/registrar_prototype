package org.perun.registrarprototype.core.services.modules;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.extension.dto.ApplicationDto;
import org.perun.registrarprototype.extension.dto.CurrentUserDto;
import org.perun.registrarprototype.extension.dto.FormItemDataDto;
import org.perun.registrarprototype.extension.dto.FormType;
import org.perun.registrarprototype.extension.services.modules.FormModule;
import org.pf4j.Extension;

@Extension
public class SampleModule implements FormModule {
  @Override
  public String getName() {
    return "SampleModule";
  }

  @Override
  public void canBeSubmitted(CurrentUserDto sess, FormType type, Map<String, String> options) {

  }

  @Override
  public void afterFormItemsPrefilled(CurrentUserDto sess, FormType type, List<FormItemDataDto> prefilledFormItems) {

  }

  @Override
  public void afterApplicationSubmitted(ApplicationDto application) {

  }

  @Override
  public void beforeApproval(ApplicationDto application) {

  }

  @Override
  public void onApproval(ApplicationDto application) {

  }

  @Override
  public void onAutoApproval(ApplicationDto application) {

  }

  @Override
  public void onRejection(ApplicationDto application) {

  }

  @Override
  public List<String> getRequiredOptions() {
    return List.of();
  }
}
