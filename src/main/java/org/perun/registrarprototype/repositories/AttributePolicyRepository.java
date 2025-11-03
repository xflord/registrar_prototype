package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.AttributePolicy;

public interface AttributePolicyRepository {
  AttributePolicy save(AttributePolicy attributePolicy);
  List<AttributePolicy> findAll();
  Optional<AttributePolicy> findByUrn(String urn);
  void delete(AttributePolicy attributePolicy);
}
