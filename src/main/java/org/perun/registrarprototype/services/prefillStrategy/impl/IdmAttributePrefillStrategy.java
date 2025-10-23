package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

@Component
public class IdmAttributePrefillStrategy implements PrefillStrategy {

  private final IdMService idmService;
  private final SessionProvider sessionProvider;
  public IdmAttributePrefillStrategy(IdMService idmService, SessionProvider sessionProvider) {
    this.sessionProvider = sessionProvider;
    this.idmService = idmService;
  }

  @Override
  public Optional<String> prefill(FormItem item, Map<String, Object> config) {

    Integer voId = null;
    Integer groupId = null;
    if (!config.containsKey("groupId")) {
      if (!config.containsKey("voId")) {
        throw new IllegalArgumentException("Missing groupId or voId config");
      }
      if ( ! (config.get("voId") instanceof Integer)) {
        throw new IllegalArgumentException("voId config must be an integer");
      }
      voId = (Integer) config.get("voId");
    } else {
      if (! (config.get("groupId") instanceof Integer)) {
        throw new IllegalArgumentException("groupId config must be an integer");
      }
      groupId = (Integer) config.get("groupId");
    }

    if (voId == null && groupId == null) {
      throw new IllegalArgumentException("Missing groupId or voId config");
    }

    if (!config.containsKey("sourceAttribute")) {
      throw new IllegalArgumentException("Missing sourceAttr config");
    }

    String sourceAttr = (String) config.get("sourceAttribute");
    // TODO warn if vo/group not present but required for the attribute
    if (sourceAttr.startsWith(idmService.getUserAttributeUrn())) {
      return Optional.ofNullable(
          idmService.getUserAttribute(sessionProvider.getCurrentSession().getPrincipal().id(), sourceAttr));
    } else if (sourceAttr.startsWith(idmService.getVoAttributeUrn())) {
      return Optional.ofNullable(idmService.getVoAttribute(sourceAttr, voId));
    } else if (sourceAttr.startsWith(idmService.getMemberAttributeUrn()) && groupId != null) { // TODO can get member attr just from voId
      return Optional.ofNullable(
          idmService.getMemberAttribute(sessionProvider.getCurrentSession().getPrincipal().id(), sourceAttr, groupId));
    } else if (sourceAttr.startsWith(idmService.getGroupAttributeUrn()) && groupId != null) { // TODO better check if group is present
      return Optional.ofNullable(idmService.getGroupAttribute(sourceAttr, groupId));
    } else {
      throw new IllegalArgumentException("Unsupported attribute source: " + sourceAttr);
    }
  }
}
