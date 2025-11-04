package org.perun.registrarprototype.services;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.perun.registrarprototype.models.AttributePolicy;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.repositories.AttributePolicyRepository;
import org.perun.registrarprototype.repositories.FormItemRepository;
import org.perun.registrarprototype.services.loaders.AttributePolicyYamlLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AttributePolicyServiceImpl implements AttributePolicyService {
  private final AttributePolicyRepository attributePolicyRepository;
  private final AttributePolicyYamlLoader attributePolicyYamlLoader;
  private final FormItemRepository formItemRepository;
  @Value( "${attribute.policy.yaml.path}" )
  private String configPath;

  public AttributePolicyServiceImpl(AttributePolicyRepository attributePolicyRepository,
                                    AttributePolicyYamlLoader attributePolicyYamlLoader,
                                    FormItemRepository formItemRepository) {
    this.attributePolicyRepository = attributePolicyRepository;
    this.attributePolicyYamlLoader = attributePolicyYamlLoader;
    this.formItemRepository = formItemRepository;
  }

  @PostConstruct
  public void initializePolicies() {
    List<AttributePolicy> policiesFromYamlConfig = attributePolicyYamlLoader.load(configPath);

    policiesFromYamlConfig.forEach(attributePolicy -> {
      if (attributePolicyRepository.findByUrn(attributePolicy.getUrn()).isEmpty()) {
        validateAttributePolicyTexts(attributePolicy);
        validateAttributePolicySourceOptions(attributePolicy);
        validateAttributePolicyConsistentPrefillOptions(attributePolicy);
        attributePolicyRepository.save(attributePolicy); // TODO could be joined into one query ?
        System.out.println("Created attribute policy " + attributePolicy.getUrn() + " from YAML config.");
      } else {
        System.out.println("Skipping + " + attributePolicy.getUrn() + " because it already exists.");
      }
    });
    // now that all policies are saved, check that source attribute policies exist

    attributePolicyRepository.findAll().forEach(this::validateAttributePolicyExistentSources);
  }

  @Override
  public List<AttributePolicy> getAttributePolicies() {
    return attributePolicyRepository.findAll();
  }

  @Override
  public AttributePolicy getAttributePolicy(String urn) {
    return attributePolicyRepository.findByUrn(urn)
               .orElseThrow(() -> new IllegalArgumentException("Attribute policy not found: " + urn));
  }

  @Override
  public AttributePolicy createAttributePolicy(AttributePolicy attributePolicy) {
    validateAttributePolicyTexts(attributePolicy);
    validateAttributePolicyExistentSources(attributePolicy);
    validateAttributePolicyConsistentPrefillOptions(attributePolicy);
    validateAttributePolicySourceOptions(attributePolicy);
    return attributePolicyRepository.save(attributePolicy);
  }

  @Override
  public AttributePolicy updateAttributePolicy(AttributePolicy attributePolicy) {
    validateAttributePolicyTexts(attributePolicy);
    validateAttributePolicyExistentSources(attributePolicy);
    validateAttributePolicyConsistentPrefillOptions(attributePolicy);
    validateAttributePolicySourceOptions(attributePolicy);
    if (!formItemRepository.getFormItemsByDestinationAttribute(attributePolicy.getUrn()).isEmpty()) {
      // TODO offer some sort of solution in this case, maybe check also if there are existing applications with this item
      throw new IllegalArgumentException("Cannot edit an attribute policy which is used as destination in some forms.");
    }
    // TODO do we detect source attributes as well? Depends on if we decide that they should enforce item settings as well.
    AttributePolicy existingPolicy = getAttributePolicy(attributePolicy.getUrn());
    if (existingPolicy.getId() != attributePolicy.getId()) {
      throw new IllegalArgumentException("ID and URN of the updated attribute policy are not the same as existing policy");
    }
    return attributePolicyRepository.save(attributePolicy);
  }

  @Override
  public void deleteAttributePolicy(String urn) {
    AttributePolicy attributePolicy = getAttributePolicy(urn);
    attributePolicyRepository.delete(attributePolicy);
  }

  @Override
  public List<AttributePolicy> getAllowedDestinationsForType(FormItem.Type type) {
    return attributePolicyRepository.findAll().stream()
               .filter(policy -> policy.isAllowAsDestination() &&
                                     (policy.getAllowedItemTypes() == null || policy.getAllowedItemTypes().contains(type)))
               .toList();
  }

  @Override
  public List<AttributePolicy> getAllowedSourceAttributesForDestination(String destinationUrn) {
    AttributePolicy policy = attributePolicyRepository.findByUrn(destinationUrn)
                                 .orElseThrow(() -> new IllegalArgumentException("Attribute policy not found: " + destinationUrn));
    List<AttributePolicy> sourcePolicies = new ArrayList<>();
    policy.getAllowedSourceAttributes().forEach(sourcePolicy -> {
      sourcePolicies.add(attributePolicyRepository.findByUrn(sourcePolicy)
                             .orElseThrow(() -> new IllegalStateException("Source attribute policy not found: " + sourcePolicy)));
    });
    return sourcePolicies;
  }

  @Override
  public void applyAttributePolicyToItem(FormItem formItem) {
    // TODO probably better way to keep source attributes?
    List<String> itemSources = formItem.getPrefillStrategyOptions().stream()
                                   .map(PrefillStrategyEntry::getSourceAttribute)
                                   .toList();

    // check source attribute validity
    String destinationAttribute = formItem.getDestinationIdmAttribute();
    formItem.getPrefillStrategyOptions().forEach(entry -> {
      AttributePolicy sourcePolicy = attributePolicyRepository.findByUrn(entry.getSourceAttribute()).orElse(null);

      if (sourcePolicy == null || !sourcePolicy.isAllowAsSource()) {
        if (destinationAttribute == null) {
          throw new IllegalArgumentException("Source attribute " + entry.getSourceAttribute() + " is not allowed as source attribute");
        }
        AttributePolicy destinationPolicy = getAttributePolicy(destinationAttribute);
        if (!destinationPolicy.getAllowedSourceAttributes().contains(entry.getSourceAttribute())) {
          throw new IllegalArgumentException("Source attribute " + entry.getSourceAttribute() + " is not allowed as source attribute");
        }
      } else if (!entry.getPrefillStrategyType().equals(sourcePolicy.getSourcePrefillStrategy())) {
        throw new IllegalArgumentException("Source attribute " + entry.getSourceAttribute() + " is not allowed as source for prefill strategy " + entry.getPrefillStrategyType());
      }
    });

    if (formItem.getDestinationIdmAttribute() == null) return;

    // check destination attribute validity
    AttributePolicy policy = getAttributePolicy(destinationAttribute);
    if (!policy.isAllowAsDestination()) {
      throw new IllegalArgumentException("Destination attribute is not allowed for form item " + formItem);
    }

    if (policy.getAllowedItemTypes() != null && !policy.getAllowedItemTypes().contains(formItem.getType())) {
      throw new IllegalArgumentException("Item type " + formItem.getType() +" is not allowed for destination attribute " + policy.getUrn() );
    }

    if (policy.getEnforcedUpdatable() != null && !policy.getEnforcedUpdatable().equals(formItem.isUpdatable())) {
      throw new IllegalArgumentException("Item has to have updatable set as " + policy.getEnforcedUpdatable() + " for destination attribute " + policy.getUrn());
    }

    if (policy.getEnforcedRequired() != null && !policy.getEnforcedRequired().equals(formItem.isRequired())) {
      throw new IllegalArgumentException("Item has to have required set as " + policy.getEnforcedRequired() + " for destination attribute " + policy.getUrn());
    }

    if (policy.getEnforcedDisabled() != null && !policy.getEnforcedDisabled().equals(formItem.getDisabled())) {
      throw new IllegalArgumentException("Item has to have disabled set as " + policy.getEnforcedDisabled() + " for destination attribute " + policy.getUrn());
    }

    if (policy.getEnforcedHidden() != null && !policy.getEnforcedHidden().equals(formItem.getHidden())) {
      throw new IllegalArgumentException("Item has to have disabled set as " + policy.getEnforcedHidden() + " for destination attribute " + policy.getUrn());
    }

    if (policy.getAllowedPrefillStrategies() != null) {
      formItem.getPrefillStrategyOptions().forEach(prefillStrategyOption -> {
        if (!policy.getAllowedPrefillStrategies().contains(prefillStrategyOption.getPrefillStrategyType())) {
          throw new IllegalArgumentException("Prefill strategy `" + prefillStrategyOption.getPrefillStrategyType() +
                                                " is not allowed for destination attribute " + policy.getUrn());
        }
      });
    }


    for (Locale locale : policy.getEnforcedTexts().keySet()) {
      // TODO how to handle locales not defined in policy?
      if (policy.isEnforceLabels()) {
        if (formItem.getTexts().get(locale) == null ||
                !policy.getEnforcedTexts().get(locale).getLabel().equals(formItem.getTexts().get(locale).getLabel())) {
          throw new IllegalArgumentException(
              ("Label of item " + formItem + " does not match the one enforced by destination attribute " +
                   policy.getUrn()));
        }
      }

      if (policy.isEnforceError()) {
        if (formItem.getTexts().get(locale) == null ||
                !policy.getEnforcedTexts().get(locale).getError().equals(formItem.getTexts().get(locale).getError())) {
          throw new IllegalArgumentException(
              ("Error of item " + formItem + " does not match the one enforced by destination attribute " +
                   policy.getUrn()));
        }
      }

      if (policy.isEnforceHelp()) {
        if (formItem.getTexts().get(locale) == null ||
                !policy.getEnforcedTexts().get(locale).getHelp().equals(formItem.getTexts().get(locale).getHelp())) {
          throw new IllegalArgumentException(
              ("Help of item " + formItem + " does not match the one enforced by destination attribute " +
                   policy.getUrn()));
        }
      }
    }

      if (policy.getEnforcedPrefillOptions() != null) {
        if (formItem.getPrefillStrategyOptions().retainAll(policy.getEnforcedPrefillOptions())) {
          throw new IllegalArgumentException("Prefill options have to match those enforced by destination attribute " + policy.getUrn());
        }
      }

    itemSources.forEach(source -> {
      if (policy.getAllowedSourceAttributes() != null && !policy.getAllowedSourceAttributes().contains(source)) {
        throw new IllegalArgumentException("Prefill options contain source attributes not allowed for destination attribute " + policy.getUrn());
      }
    });
  }

  /**
   * Validates that the policy enforced texts are all filled.
   * @param attributePolicy
   */
  public void validateAttributePolicyTexts(AttributePolicy attributePolicy) {
    if (attributePolicy.isEnforceLabels() || attributePolicy.isEnforceError() || attributePolicy.isEnforceHelp()) {
      if (attributePolicy.getEnforcedTexts() == null || attributePolicy.getEnforcedTexts().isEmpty()) {
        throw new IllegalArgumentException("Texts are enforced for attribute " + attributePolicy.getUrn() +
                                               " but none are defined.");

      }
      for (Locale locale : attributePolicy.getEnforcedTexts().keySet()) {
        ItemTexts itemTexts = attributePolicy.getEnforcedTexts().get(locale);

        if (itemTexts == null) {
          throw new IllegalArgumentException("Texts are enforced for attribute " + attributePolicy.getUrn() +
                                               " but none are defined for locale " + locale);
        }

        if (attributePolicy.isEnforceLabels() && itemTexts.getLabel() == null) {
          throw new IllegalArgumentException("Labels are enforced for attribute `" + attributePolicy.getUrn() +
                                                 "` but are missing for locale " + locale);
        }

        if (attributePolicy.isEnforceHelp() && itemTexts.getHelp() == null) {
          throw new IllegalArgumentException("Hints are enforced for attribute `" + attributePolicy.getUrn() +
                                                 "` but are missing for locale " + locale);
        }

        if (attributePolicy.isEnforceError() && itemTexts.getError() == null) {
          throw new IllegalArgumentException("Errors are enforced for attribute `" + attributePolicy.getUrn() +
                                                 "` but are missing for locale " + locale);
        }
      }
    }
  }

  public void validateAttributePolicyConsistentPrefillOptions(AttributePolicy attributePolicy) {
    if (attributePolicy.getEnforcedPrefillOptions() == null) return;
    if (attributePolicy.getAllowedPrefillStrategies() == null) return;

    for (PrefillStrategyEntry prefillStrategyEntry : attributePolicy.getEnforcedPrefillOptions()) {
      if (!attributePolicy.getAllowedPrefillStrategies().contains(prefillStrategyEntry.getPrefillStrategyType())) {
        throw new IllegalArgumentException("Cannot enforce strategy " + prefillStrategyEntry.getPrefillStrategyType() + " if it is not allowed in attribute " + attributePolicy.getUrn());
      }
    }
  }

  private void validateAttributePolicySourceOptions(AttributePolicy attributePolicy) {
    if (!attributePolicy.isAllowAsSource()) return;
    if (attributePolicy.getSourcePrefillStrategy() == null) {
      throw new IllegalArgumentException("Source prefill strategy is required for attribute " + attributePolicy.getUrn());
    }
    if (!attributePolicy.getSourcePrefillStrategy().requiresSource()) {
      throw new IllegalArgumentException("Invalid source prefill strategy defined for " + attributePolicy.getUrn());
    }
  }

  private void validateAttributePolicyExistentSources(AttributePolicy attributePolicy) {
    attributePolicy.getAllowedSourceAttributes().forEach(allowedSourceAttribute -> {
      if (attributePolicy.getAllowedSourceAttributes().contains(allowedSourceAttribute)) {
        return;
      }
      AttributePolicy sourcePolicy = attributePolicyRepository.findByUrn(allowedSourceAttribute)
                                         .orElseThrow(() -> new IllegalArgumentException("Source attribute " +
                                                    allowedSourceAttribute + " for " + attributePolicy.getUrn() + " does not exist."));

      if (!sourcePolicy.isAllowAsSource()) {
        throw new IllegalArgumentException("Source attribute " + allowedSourceAttribute + " for " +
                                              attributePolicy.getUrn() + " is not allowed as source attribute.");
      }
      FormItem.PrefillStrategyType sourcePrefillStrategy = sourcePolicy.getSourcePrefillStrategy();
      if (sourcePrefillStrategy != null) {
        if (!attributePolicy.getAllowedPrefillStrategies().contains(sourcePrefillStrategy)) {
          System.err.println("No prefill strategy overlap between destination attribute " + attributePolicy.getUrn() +
                                 " and its source attribute " + sourcePolicy.getUrn());
        }
      }
    });
  }
}
