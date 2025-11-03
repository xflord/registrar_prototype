package org.perun.registrarprototype.services;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.perun.registrarprototype.models.AttributePolicy;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.repositories.AttributePolicyRepository;
import org.perun.registrarprototype.services.loaders.AttributePolicyYamlLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AttributePolicyServiceImpl implements AttributePolicyService {
  private final AttributePolicyRepository attributePolicyRepository;
  private final AttributePolicyYamlLoader attributePolicyYamlLoader;
  @Value( "${attribute.policy.yaml.path}" )
  private String configPath;

  public AttributePolicyServiceImpl(AttributePolicyRepository attributePolicyRepository,
                                    AttributePolicyYamlLoader attributePolicyYamlLoader) {
    this.attributePolicyRepository = attributePolicyRepository;
    this.attributePolicyYamlLoader = attributePolicyYamlLoader;
  }

  @PostConstruct
  public void initializePolicies() {
    List<AttributePolicy> policiesFromYamlConfig = attributePolicyYamlLoader.load(configPath);

    policiesFromYamlConfig.forEach(attributePolicy -> {
      if (attributePolicyRepository.findByUrn(attributePolicy.getUrn()).isEmpty()) {
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
    return attributePolicyRepository.save(attributePolicy);
  }

  @Override
  public AttributePolicy updateAttributePolicy(AttributePolicy attributePolicy) {
    validateAttributePolicyTexts(attributePolicy);
    validateAttributePolicyExistentSources(attributePolicy);
    validateAttributePolicyConsistentPrefillOptions(attributePolicy);
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
                                   .map(entry -> entry.getOptions().get("sourceAttribute"))
                                   .toList();

    itemSources.forEach(source -> {
      AttributePolicy sourcePolicy = getAttributePolicy(source);
      if (!sourcePolicy.isAllowAsSource()) {
        throw new IllegalArgumentException("Attribute " + source + " is not allowed as source attribute.");
      }
    });

    if (formItem.getDestinationIdmAttribute() == null) return;

    AttributePolicy policy = getAttributePolicy(formItem.getDestinationIdmAttribute());
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

    if (policy.getAllowedSourcePrefillStrategies() != null) {
      formItem.getPrefillStrategyOptions().forEach(prefillStrategyOption -> {
        if (!policy.getAllowedSourcePrefillStrategies().contains(prefillStrategyOption.getPrefillStrategyType())) {
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
  public static void validateAttributePolicyTexts(AttributePolicy attributePolicy) {
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

  public static void validateAttributePolicyConsistentPrefillOptions(AttributePolicy attributePolicy) {
    if (attributePolicy.getEnforcedPrefillOptions() == null) return;
    if (attributePolicy.getAllowedSourcePrefillStrategies() == null) return;

    for (PrefillStrategyEntry prefillStrategyEntry : attributePolicy.getEnforcedPrefillOptions()) {
      if (!attributePolicy.getAllowedSourcePrefillStrategies().contains(prefillStrategyEntry.getPrefillStrategyType())) {
        throw new IllegalArgumentException("Cannot enforce strategy " + prefillStrategyEntry.getPrefillStrategyType() + " if it is not allowed in attribute " + attributePolicy.getUrn());
      }
    }
  }

  private void validateAttributePolicyExistentSources(AttributePolicy attributePolicy) {
    attributePolicy.getAllowedSourceAttributes().forEach(allowedSourceAttribute -> {
      AttributePolicy sourcePolicy = attributePolicyRepository.findByUrn(allowedSourceAttribute)
                                         .orElseThrow(() -> new IllegalArgumentException("Source attribute " +
                                                    allowedSourceAttribute + " for " + attributePolicy.getUrn() + " does not exist."));

      if (!sourcePolicy.isAllowAsSource()) {
        throw new IllegalArgumentException("Source attribute " + allowedSourceAttribute + " for " +
                                              attributePolicy.getUrn() + " is not allowed as source attribute.");
      }
      Set<FormItem.PrefillStrategyType> allowedSourceStrategies = sourcePolicy.getAllowedSourcePrefillStrategies();
      if (allowedSourceStrategies != null) { //null means any
        allowedSourceStrategies.retainAll(attributePolicy.getAllowedDestinationPrefillStrategies());
        if (allowedSourceStrategies.isEmpty()) {
          System.err.println("No prefill strategy overlap between destination attribute " + attributePolicy.getUrn() +
                                 " and its source attribute " + sourcePolicy.getUrn());
        }
      }
    });
  }
}
