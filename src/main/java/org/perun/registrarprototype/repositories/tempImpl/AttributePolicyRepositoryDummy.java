package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.AttributePolicy;
import org.perun.registrarprototype.repositories.AttributePolicyRepository;
import org.springframework.stereotype.Component;

@Component
public class AttributePolicyRepositoryDummy implements AttributePolicyRepository {
  private static final List<AttributePolicy> attributePolicies = new ArrayList<>();
  private static int currId = 1;

  @Override
  public AttributePolicy save(AttributePolicy attributePolicy) {
    if (attributePolicy.getId() > 0) {
      boolean removed = attributePolicies.removeIf(policy -> policy.getId() == attributePolicy.getId());
      attributePolicies.add(attributePolicy);
      if (removed) {
        System.out.println("Updated attribute policy " + attributePolicy.getId());
      } else {
        System.out.println("Created attribute policy " + attributePolicy.getId() + " (with existing ID)");
      }
      return attributePolicy;
    }

    attributePolicy.setId(currId++);
    attributePolicies.add(attributePolicy);
    System.out.println("Created attribute policy " + attributePolicy.getId());
    return attributePolicy;
  }

  @Override
  public List<AttributePolicy> findAll() {
    return attributePolicies;
  }

  @Override
  public Optional<AttributePolicy> findByUrn(String urn) {
    return attributePolicies.stream()
               .filter(attributePolicy -> attributePolicy.getUrn().equals(urn))
               .findFirst();
  }

  @Override
  public void delete(AttributePolicy attributePolicy) {
    attributePolicies.remove(attributePolicy);
  }
}
