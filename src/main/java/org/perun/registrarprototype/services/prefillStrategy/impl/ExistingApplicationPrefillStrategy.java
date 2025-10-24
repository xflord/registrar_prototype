package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

@Component
public class ExistingApplicationPrefillStrategy implements PrefillStrategy {
  @Override
  public Optional<String> prefill(FormItem item, Map<String, String> config) {
    return Optional.empty();
  }

  @Override
  public void validateOptions(Map<String, String> options) {

  }

  @Override
  public FormItem.PrefillStrategyType getType() {
    return FormItem.PrefillStrategyType.APPLICATION;
  }
}
