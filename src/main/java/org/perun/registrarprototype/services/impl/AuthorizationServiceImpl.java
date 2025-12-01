package org.perun.registrarprototype.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.persistance.DestinationRepository;
import org.perun.registrarprototype.persistance.FormRepository;
import org.perun.registrarprototype.persistance.PrefillStrategyEntryRepository;
import org.perun.registrarprototype.persistance.SubmissionRepository;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.services.AuthorizationService;

import org.springframework.stereotype.Service;

// Role-based access control implementation
@Service
public class AuthorizationServiceImpl implements AuthorizationService {

  private final FormRepository formRepository;
  private final DestinationRepository destinationRepository;
  private final PrefillStrategyEntryRepository prefillStrategyEntryRepository;
  private final SubmissionRepository submissionRepository;

  public AuthorizationServiceImpl(FormRepository formRepository, DestinationRepository destinationRepository,
                                  PrefillStrategyEntryRepository prefillStrategyEntryRepository,
                                  SubmissionRepository submissionRepository) {
    this.formRepository = formRepository;
    this.destinationRepository = destinationRepository;
    this.prefillStrategyEntryRepository = prefillStrategyEntryRepository;
    this.submissionRepository = submissionRepository;
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, String groupId) {
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (isAdmin(sess)) {
      return true;
    }

    return sess.getPrincipal().getRoles().get(Role.FORM_MANAGER).contains(groupId);
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, Application app) {
    // TODO verify this is all the applicable conditions
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (isAdmin(sess)) {
      return true;
    }

    if (app.getIdmUserId() != null && Objects.equals(app.getIdmUserId(), sess.getPrincipal().id())) {
      return true;
    }

    // Load the submission using the submissionId
    return submissionRepository.findById(app.getSubmissionId())
        .map(submission -> Objects.equals(submission.getIdentityIssuer(), sess.getPrincipal().attribute("iss")) &&
               Objects.equals(submission.getIdentityIdentifier(), sess.getPrincipal().attribute("sub")))
        .orElse(false);
  }

  @Override
  public boolean canManage(RegistrarAuthenticationToken sess, ItemDefinition itemDefinition) {
    if (itemDefinition.isGlobal()) {
      return isAdmin(sess);
    } else {
      // Check if destination is global
      if (itemDefinition.getDestinationId() != null) {
        Optional<Destination> destination = destinationRepository.findById(itemDefinition.getDestinationId());
        if (destination.isPresent() && destination.get().isGlobal()) {
          return isAdmin(sess);
        }
      }
      
      // Check if any prefill strategy is global
      if (itemDefinition.getPrefillStrategyIds() != null && !itemDefinition.getPrefillStrategyIds().isEmpty()) {
        List<PrefillStrategyEntry> prefillStrategies = prefillStrategyEntryRepository.findAllById(itemDefinition.getPrefillStrategyIds());
        for (PrefillStrategyEntry entry : prefillStrategies) {
          if (entry.isGlobal()) {
            return isAdmin(sess);
          }
        }
      }
      
      // Check form specification
      if (itemDefinition.getFormSpecificationId() == null) {
        throw new IllegalArgumentException("Form specification ID is null");
      }
      
      // Authorization check
      Optional<FormSpecification> formSpecification = formRepository.findById(itemDefinition.getFormSpecificationId());
      if (formSpecification.isPresent()) {
        return canManage(sess, formSpecification.get().getGroupId());
      } else {
        throw new IllegalArgumentException("Form specification not found for ID: " + itemDefinition.getFormSpecificationId());
      }
    }
  }

  @Override
  public boolean canDecide(RegistrarAuthenticationToken sess, String groupId) {
    if (!sess.isAuthenticated()) {
      return false;
    }

    if (isAdmin(sess)) {
      return true;
    }
//    return canManage(sess, groupId) || sess.getPrincipal().getRoles().get(Role.FORM_APPROVER).contains(groupId);
    return sess.getPrincipal().getRoles().get(Role.FORM_APPROVER).contains(groupId);
  }

  @Override
  public boolean isAdmin(RegistrarAuthenticationToken sess) {
    return sess.getPrincipal().getRoles().containsKey(Role.ADMIN);
  }

}
