package org.perun.registrarprototype.services.config;

import cz.metacentrum.perun.openapi.model.AttributeDefinition;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.services.idmIntegration.IdMService;

public class IdMServiceDummy implements IdMService {
  @Override
  public Integer getUserIdByIdentifier(String identifier) {
    return 0;
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
  public Map<Role, Set<Integer>> getRolesByUserId(Integer userId) {
    return Map.of();
  }

  @Override
  public String getUserAttribute(Integer userId, String attributeName) {
    return "";
  }

  @Override
  public String getMemberAttribute(Integer userId, String attributeName, Integer groupId) {
    return "";
  }

  @Override
  public String getMemberGroupAttribute(Integer userId, String attributeName, Integer groupId) {
    return "";
  }

  @Override
  public boolean checkGroupExists(Integer groupId) {
    return false;
  }

  @Override
  public boolean canExtendMembership(Integer userId, Integer groupId) {
    return false;
  }

  @Override
  public String getVoAttribute(String attributeName, int voId) {
    return "";
  }

  @Override
  public String getGroupAttribute(String attributeName, Integer groupId) {
    return "";
  }

  @Override
  public AttributeDefinition getAttributeDefinition(String attributeName) {
    return null;
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
  public Map<String, String> getReservedLoginsForApplication(Application application) {
    return Map.of();
  }

  @Override
  public boolean doesUserHaveExistingLoginSet(Integer userId, String namespace) {
    return false;
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
  public String getLoginAttributeUrn() {
    return "";
  }

  @Override
  public String getUserAttributeUrn() {
    return "";
  }

  @Override
  public String getMemberAttributeUrn() {
    return "";
  }

  @Override
  public String getGroupAttributeUrn() {
    return "";
  }

  @Override
  public String getVoAttributeUrn() {
    return "";
  }

  @Override
  public Integer createMemberForCandidate(Application application) {
    return 0;
  }

  @Override
  public Integer createMemberForUser(Application application) {
    return 0;
  }

  @Override
  public Integer extendMembership(Application application) {
    return 0;
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
