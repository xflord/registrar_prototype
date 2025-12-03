package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.persistence.DestinationRepository;
import org.perun.registrarprototype.persistence.ItemDefinitionRepository;
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
  private final ItemDefinitionRepository itemDefinitionRepository;
  private final DestinationRepository destinationRepository;

  public LoginItemPrefillStrategy(IdMService idmService, SessionProvider sessionProvider,
                                  ItemDefinitionRepository itemDefinitionRepository,
                                  DestinationRepository destinationRepository) {
    this.idmService = idmService;
    this.sessionProvider = sessionProvider;
    this.itemDefinitionRepository = itemDefinitionRepository;
    this.destinationRepository = destinationRepository;
  }


  @Override
  public Optional<String> prefill(FormItem item, PrefillStrategyEntry entry) {
    // Get the ItemDefinition using the itemDefinitionId
    ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getItemDefinitionId())
        .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for id: " + item.getItemDefinitionId()));
    
    if (!itemDefinition.getType().equals(ItemType.LOGIN)) {
      throw new IllegalStateException("This strategy can only be used for LOGIN items");
    }

    Map<String, String> reservedLogins = getReservedLoginsForPrincipal(sessionProvider.getCurrentSession()
                                                                           .getPrincipal());

    for (String reservedNamespace : reservedLogins.keySet()) {
      String loginAttributeDefinition = idmService.getLoginAttributeUrn() + reservedNamespace;
      
      // Get the Destination using the destinationId
      Destination destination = destinationRepository.findById(itemDefinition.getDestinationId())
          .orElseThrow(() -> new IllegalStateException("Destination not found for id: " + itemDefinition.getDestinationId()));
          
      if (destination.getUrn().equals(loginAttributeDefinition)) {
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
