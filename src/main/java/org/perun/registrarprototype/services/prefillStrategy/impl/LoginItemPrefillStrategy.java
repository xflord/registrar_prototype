package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

@Component
public class LoginItemPrefillStrategy implements PrefillStrategy {

  private final IdMService idmService;

  public LoginItemPrefillStrategy(IdMService idmService) {
    this.idmService = idmService;
  }


  @Override
  public Optional<String> prefill(FormItem item, Map<String, Object> config) {
    if (!config.containsKey("reservedLogins")) {
      throw new IllegalArgumentException("Missing reservedLogins config");
    }
    if ( ! (config.get("reservedLogins") instanceof Map)) {
      throw new IllegalArgumentException("reservedLogins config must be a map");
    }

    if (!config.containsKey("namespace")) {
      throw new IllegalArgumentException("Missing namespace config");
    }

    if ( ! ( config.get("namespace") instanceof String)) {
      throw new IllegalArgumentException("namespace config must be a string");
    }

    Map<String, String> reservedLogins = (Map<String, String>) config.get("reservedLogins");
    String namespace = (String) config.get("namespace");

    for (String reservedNamespace : reservedLogins.keySet()) {
      String loginAttributeDefinition = idmService.getLoginAttributeUrn() + reservedNamespace;
      if (item.getDestinationIdmAttribute().equals(loginAttributeDefinition)) {
        return Optional.of(reservedLogins.get(reservedNamespace));
      }
    }
    return Optional.empty();
  }
}
