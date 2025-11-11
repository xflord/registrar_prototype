package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

@Component
public class ExistingApplicationPrefillStrategy implements PrefillStrategy {
  @Override
  public Optional<String> prefill(FormItem item, PrefillStrategyEntry entry) {
    return Optional.empty();
  }

  @Override
  public void validateOptions(PrefillStrategyEntry entry) {

  }

  @Override
  public PrefillStrategyEntry.PrefillStrategyType getType() {
    return PrefillStrategyEntry.PrefillStrategyType.APPLICATION;
  }
}
