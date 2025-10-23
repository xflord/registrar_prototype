package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

@Component
public class IdentityAttributePrefillStrategy implements PrefillStrategy {

  private final SessionProvider sessionProvider;
  public IdentityAttributePrefillStrategy(SessionProvider sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  public Optional<String> prefill(FormItem item, Map<String, Object> config) {

    if (!config.containsKey("sourceAttribute")) {
      throw new IllegalArgumentException("Missing sourceAttribute config");
    }
    String sourceAttr = (String) config.get("sourceAttribute");

    String attrValue = sessionProvider.getCurrentSession().getPrincipal().attribute(sourceAttr);

    return Optional.ofNullable(attrValue);
  }
}
