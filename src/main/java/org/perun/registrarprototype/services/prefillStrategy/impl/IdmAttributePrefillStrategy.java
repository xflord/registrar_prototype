package org.perun.registrarprototype.services.prefillStrategy.impl;

import io.micrometer.common.util.StringUtils;
import java.util.Optional;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.persistance.FormRepository;
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
  public Optional<String> prefill(FormItem item, PrefillStrategyEntry entry) {
    Integer formSpecificationId = item.getFormSpecificationId();
    if (formSpecificationId == null) {
      throw new DataInconsistencyException("Form item does not have a valid form specification ID");
    }
    
    Optional<FormSpecification> formSpecificationOpt = formRepository.findById(formSpecificationId);
    if (formSpecificationOpt.isEmpty()) {
      throw new DataInconsistencyException("Form specification with ID " + formSpecificationId + " not found");
    }
    
    FormSpecification formSpecification = formSpecificationOpt.get();


    String sourceAttr = entry.getSourceAttribute();
    // TODO warn if vo/group not present but required for the attribute
    try {
      return Optional.ofNullable(idmService.getAttribute(sourceAttr, sessionProvider.getCurrentSession().getPrincipal().id(),
          formSpecification.getGroupId(), formSpecification.getVoId()));
    } catch (IdmAttributeNotExistsException e) {
      // TODO login attribute definition was removed in IdM between submission and approval, alert admins?
      throw new IllegalStateException("Login item attribute has been deleted in the underlying IdM system, " +
                                         e.getMessage());
    }
  }

  @Override
  public void validateOptions(PrefillStrategyEntry entry) {
    if (StringUtils.isEmpty(entry.getSourceAttribute())) {
          throw new IllegalArgumentException("Missing sourceAttribute config");
        }
  }

  @Override
  public PrefillStrategyEntry.PrefillStrategyType getType() {
    return PrefillStrategyEntry.PrefillStrategyType.IDM_ATTRIBUTE;
  }
}
