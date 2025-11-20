package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

/**
 * Strategy specifically for login items, prefills an already reserved login (for the destination namespace) if it exists
 */
@Component
public class LoginItemPrefillStrategy implements PrefillStrategy {

  private final IdMService idmService;
  private final SessionProvider sessionProvider;

  public LoginItemPrefillStrategy(IdMService idmService, SessionProvider sessionProvider) {
    this.idmService = idmService;
    this.sessionProvider = sessionProvider;
  }


  @Override
  public Optional<String> prefill(FormItem item, PrefillStrategyEntry entry) {
    if (!item.getItemDefinition().getType().equals(ItemType.LOGIN)) {
      throw new IllegalStateException("This strategy can only be used for LOGIN items");
    }

    Map<String, String> reservedLogins = getReservedLoginsForPrincipal(sessionProvider.getCurrentSession()
                                                                           .getPrincipal());

    for (String reservedNamespace : reservedLogins.keySet()) {
      String loginAttributeDefinition = idmService.getLoginAttributeUrn() + reservedNamespace;
      if (item.getItemDefinition().getDestination().getUrn().equals(loginAttributeDefinition)) {
        return Optional.of(reservedLogins.get(reservedNamespace));
      }
    }
    return Optional.empty();
  }

  @Override
  public void validateOptions(PrefillStrategyEntry entry) {

  }

  @Override
  public PrefillStrategyEntry.PrefillStrategyType getType() {
    return PrefillStrategyEntry.PrefillStrategyType.LOGIN_ATTRIBUTE;
  }

  /**
   * Returns all logins reserved by the authenticated principal
   * @param principal
   * @return a map of reserved logins, the keys being namespaces of the logins
   */
  private Map<String, String> getReservedLoginsForPrincipal(CurrentUser principal) {
    if (principal == null) {
      return new HashMap<>();
    }
    return new HashMap<>();
  }
}
