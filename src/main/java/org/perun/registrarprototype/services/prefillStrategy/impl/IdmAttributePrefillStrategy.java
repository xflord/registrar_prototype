package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

/**
 * Retrieves value of item's source IdM attribute.
 */
@Component
public class IdmAttributePrefillStrategy implements PrefillStrategy {

  private final IdMService idmService;
  private final SessionProvider sessionProvider;
  private final FormRepository formRepository;
  public IdmAttributePrefillStrategy(IdMService idmService, SessionProvider sessionProvider, FormRepository formRepository) {
    this.sessionProvider = sessionProvider;
    this.idmService = idmService;
    this.formRepository = formRepository;
  }

  @Override
  public Optional<String> prefill(FormItem item, Map<String, String> options) {
    FormSpecification
        formSpecification = formRepository.findById(item.getFormId())
                                .orElseThrow(() -> new DataInconsistencyException("Form with ID " + item.getFormId() + " not found for form item " + item.getId()));


    String sourceAttr = options.get("sourceAttribute");
    // TODO warn if vo/group not present but required for the attribute
    try {
      if (sourceAttr.startsWith(idmService.getUserAttributeUrn())) {
        return Optional.ofNullable(
            idmService.getUserAttribute(sessionProvider.getCurrentSession().getPrincipal().id(), sourceAttr));
      } else if (sourceAttr.startsWith(idmService.getVoAttributeUrn())) {
        return Optional.ofNullable(idmService.getVoAttribute(sourceAttr, formSpecification.getVoId()));
      } else if (sourceAttr.startsWith(idmService.getMemberAttributeUrn()) &&
                     formSpecification.getGroupId() != null) { // TODO can get member attr just from voId
        return Optional.ofNullable(
            idmService.getMemberAttribute(sessionProvider.getCurrentSession().getPrincipal().id(), sourceAttr,
                formSpecification.getGroupId()));
      } else if (sourceAttr.startsWith(idmService.getGroupAttributeUrn()) &&
                     formSpecification.getGroupId() != null) { // TODO better check if group is present
        return Optional.ofNullable(idmService.getGroupAttribute(sourceAttr, formSpecification.getGroupId()));
      } else {
        throw new IllegalArgumentException("Unsupported attribute source: " + sourceAttr);
      }
    } catch (IdmAttributeNotExistsException e) {
      // TODO login attribute definition was removed in IdM between submission and approval, alert admins?
      throw new IllegalStateException("Login item attribute has been deleted in the underlying IdM system, " +
                                         e.getMessage());
    }
  }

  @Override
  public void validateOptions(Map<String, String> options) {
    if (!options.containsKey("sourceAttribute")) {
          throw new IllegalArgumentException("Missing sourceAttribute config");
        }
  }

  @Override
  public FormItem.PrefillStrategyType getType() {
    return FormItem.PrefillStrategyType.IDM_ATTRIBUTE;
  }
}
