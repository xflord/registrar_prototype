package org.perun.registrarprototype.services.idmIntegration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Role;

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
  String getUserIdByIdentifier(String issuer, String identifier);

  List<Integer> getGroupIdsWhereUserIsMember(Integer userId);

  /**
   *
   * @param userId
   * @return Map of authorized objects (e.g. Vos, Groups) and their IDs. Keys are `Group` and `VO`.
   */
  Map<String, List<Integer>> getAuthorizedObjects(Integer userId);

  Map<Role, Set<String>> getRolesByUserId(String userId);

  String getAttribute(String attributeName, String userId, String groupId, String voId) throws IdmAttributeNotExistsException;

  String getLoginAttributeUrn();

  boolean checkGroupExists(String groupId);

  boolean canExtendMembership(String userId, String groupId);

  boolean isLoginAvailable(String namespace, String login);

  void reserveLogin(String namespace, String login);
  void releaseLogin(String namespace, String login);

  void reservePassword(String namespace, String login, String password);
  void validatePassword(Integer userId, String namespace);
  void deletePassword(String namespace, String login);

  String createMemberForCandidate(Application application);

  String createMemberForUser(Application application);

  String extendMembership(Application application);

  /**
   * Retrieves User objects that match the attributes of the oauth principal (e.g. sub in oidc, email. etc.).
   * Same situation as in getUserByIdentifier.
   *
   * @return
   */
  List<Identity> checkForSimilarUsers(String accessToken);

  List<Identity> checkForSimilarUsers(String accessToken, List<FormItemData> itemData);
}
