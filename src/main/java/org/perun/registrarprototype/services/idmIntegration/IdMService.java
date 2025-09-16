package org.perun.registrarprototype.services.idmIntegration;

import cz.metacentrum.perun.openapi.model.Identity;
import cz.metacentrum.perun.openapi.model.User;
import java.util.List;
import java.util.Map;

/**
 * Interface for interaction with the underlying IdM system (currently Perun/MidPoint?).
 *
 * Ideally, this would be an abstraction layer over the IdM system, so that it can be easily
 * replaced with a different IdM system.
 *
 * The registrar flow should pass even when all the IdM calls fail (basic use case being that the applying user does not exist in
 * the IdM system yet, the approver has roles defined in registrar - adding user to the group/Vo should be independent
 * of this interface, being done asynchronously via Events, or synchronously via Modules?)
 */
public interface IdMService {

  /**
   * Retrieve user by identifier from oauth (e.g. sub in oidc). Depending on whether we intend on implementing multiple
   * IdM systems / we want to stay as loosely coupled as possible, replace Perun User object with a custom one.
   *
   * Depending on whether we decide to save the User object (e.g. in the principal), the remaining methods can take userId as
   * argument to avoid additional calls.
   *
   * @param identifier oauth identifier (e.g. sub in oidc)
   * @return either Perun User or custom User object
   */
  User getUserByIdentifier(String identifier) throws Exception;

  List<Integer> getGroupIdsWhereUserIsMember(String identifier);

  /**
   *
   * @param identifier
   * @return Map of authorized objects (e.g. Vos, Groups) and their IDs. Keys are `Group` and `VO`.
   * @throws Exception
   */
  Map<String, List<Integer>> getAuthorizedObjects(String identifier) throws Exception;

  // TODO add custom exception for IdM errors? an ErrorHandler would be nice to have
  String getUserAttribute(String identifier, String attributeName) throws Exception;

  String getMemberAttribute(String identifier, String attributeName, int groupId);

  /**
   * Retrieves User objects that match the attributes of the oauth principal (e.g. sub in oidc, email. etc.).
   * Same situation as in getUserByIdentifier.
   *
   * @param attributes
   * @return
   */
  List<Identity> getSimilarUsers(Map<String, Object> attributes);
}
