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
public class CompositePrefillStrategy implements PrefillStrategy {

  private final List<PrefillStrategy> strategies;
  public CompositePrefillStrategy(List<PrefillStrategy> strategies) {
    this.strategies = strategies;
  }

  @Override
  public Optional<String> prefill(FormItem item, Map<String, String> options) {
    for (PrefillStrategy strategy : strategies) {
      Optional<String> result = strategy.prefill(item, strategy.getTypeSpecificOptions(options));
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }

  @Override
  public void validateOptions(Map<String, String> options) {
    for (PrefillStrategy strategy : strategies) {
      strategy.validateOptions(strategy.getTypeSpecificOptions(options));
    }
  }

  @Override
  public FormItem.PrefillStrategyType getType() {
    return null;
  }
}
