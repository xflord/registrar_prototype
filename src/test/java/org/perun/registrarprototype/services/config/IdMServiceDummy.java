package org.perun.registrarprototype.services.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.services.idmIntegration.IdMService;

public class IdMServiceDummy implements IdMService {
  @Override
  public String getUserIdByIdentifier(String issuer, String identifier) {
    return "";
  }

  @Override
  public List<Integer> getGroupIdsWhereUserIsMember(Integer userId) {
    return List.of();
  }

  @Override
  public Map<String, List<Integer>> getAuthorizedObjects(Integer userId) {
    return Map.of();
  }

  @Override
  public Map<Role, Set<String>> getRolesByUserId(String userId) {
    return Map.of();
  }

  @Override
  public String getAttribute(String attributeName, String userId, String groupId, String voId)
      throws IdmAttributeNotExistsException {
    return "";
  }

  @Override
  public String getLoginAttributeUrn() {
    return "";
  }

  @Override
  public boolean checkGroupExists(String groupId) {
    return false;
  }

  @Override
  public boolean canExtendMembership(String userId, String groupId) {
    return false;
  }

  @Override
  public boolean isLoginAvailable(String namespace, String login) {
    return false;
  }

  @Override
  public void reserveLogin(String namespace, String login) {

  }

  @Override
  public void releaseLogin(String namespace, String login) {

  }

  @Override
  public void reservePassword(String namespace, String login, String password) {

  }

  @Override
  public void validatePassword(Integer userId, String namespace) {

  }

  @Override
  public void deletePassword(String namespace, String login) {

  }

  @Override
  public String createMemberForCandidate(Application application) {
    return "";
  }

  @Override
  public String createMemberForUser(Application application) {
    return "";
  }

  @Override
  public String extendMembership(Application application) {
    return "";
  }

  @Override
  public List<Identity> checkForSimilarUsers(String accessToken) {
    return List.of();
  }

  @Override
  public List<Identity> checkForSimilarUsers(String accessToken, List<FormItemData> itemData) {
    return List.of();
  }
}
