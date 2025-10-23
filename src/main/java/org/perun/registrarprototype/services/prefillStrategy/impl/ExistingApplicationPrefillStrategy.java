package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;

public class ExistingApplicationPrefillStrategy implements PrefillStrategy {
  @Override
  public Optional<String> prefill(FormItem item, Map<String, Object> config) {
    return Optional.empty();
  }
}
