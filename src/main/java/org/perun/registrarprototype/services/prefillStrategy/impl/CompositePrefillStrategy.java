package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;

//TODO make this a @component as well?
public class CompositePrefillStrategy implements PrefillStrategy {

  private final List<PrefillStrategy> strategies;
  public CompositePrefillStrategy(List<PrefillStrategy> strategies) {
    this.strategies = strategies;
  }

  @Override
  public Optional<String> prefill(FormItem item, Map<String, Object> config) {
    for (PrefillStrategy strategy : strategies) {
      Optional<String> result = strategy.prefill(item, config);
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }
}
