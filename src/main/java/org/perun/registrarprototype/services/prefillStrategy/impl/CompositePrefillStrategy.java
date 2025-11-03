package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;


/**
 * Allows items to define multiple prefill strategies. The first strategy that returns a value is used (make sure to
 * account for this in gui when setting the strategies).
 * It is expected that strategies are returned within this composite, even when there is only one strategy.
 */
public class CompositePrefillStrategy  {

  private final List<PrefillStrategy> strategies;
  private final Map<PrefillStrategy, Map<String, String>> options;
  public CompositePrefillStrategy(List<PrefillStrategy> strategies, Map<PrefillStrategy, Map<String, String>> options) {
    this.strategies = strategies;
    this.options = options;
  }

  public Optional<String> prefill(FormItem item) {
    for (PrefillStrategy strategy : strategies) {
      strategy.validateOptions(options.get(strategy));
      Optional<String> result = strategy.prefill(item, options.get(strategy));
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }
}
