package org.perun.registrarprototype.services.prefillStrategy.impl;

import io.micrometer.common.util.StringUtils;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

/**
 * Strategy that retrieves value of item's source identity attribute
 */
@Component
public class IdentityAttributePrefillStrategy implements PrefillStrategy {

  private final SessionProvider sessionProvider;
  public IdentityAttributePrefillStrategy(SessionProvider sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  public Optional<String> prefill(FormItem item, PrefillStrategyEntry entry) {

    String sourceAttr = entry.getSourceAttribute();

    String attrValue = sessionProvider.getCurrentSession().getPrincipal().attribute(sourceAttr);

    return Optional.ofNullable(attrValue);
  }

  @Override
  public void validateOptions(PrefillStrategyEntry entry) {
    if (StringUtils.isEmpty(entry.getSourceAttribute())) {
      throw new IllegalArgumentException("Missing sourceAttribute config");
    }
  }

  @Override
  public PrefillStrategyEntry.PrefillStrategyType getType() {
    return PrefillStrategyEntry.PrefillStrategyType.IDENTITY_ATTRIBUTE;
  }
}
