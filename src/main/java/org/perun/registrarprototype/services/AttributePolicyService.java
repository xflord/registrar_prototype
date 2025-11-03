package org.perun.registrarprototype.services;

import java.util.List;
import org.perun.registrarprototype.models.AttributePolicy;
import org.perun.registrarprototype.models.FormItem;

public interface AttributePolicyService {

  List<AttributePolicy> getAttributePolicies();

  AttributePolicy getAttributePolicy(String urn);

  AttributePolicy createAttributePolicy(AttributePolicy attributePolicy);

  AttributePolicy updateAttributePolicy(AttributePolicy attributePolicy);

  void deleteAttributePolicy(String urn);

  List<AttributePolicy> getAllowedDestinationsForType(FormItem.Type type);

  List<AttributePolicy> getAllowedSourceAttributesForDestination(String destinationUrn);

  void applyAttributePolicyToItem(FormItem formItem);

}
