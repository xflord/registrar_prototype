package org.perun.registrarprototype.services.idmIntegration.keycloak;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


// steps to test this (hopefully nothing is missing):
//  1. run keycloak docker
//  2. create test realm
//  3. create client, assign service account roles (assign all to be sure, gather all required for the install doc)
//  4. generate client secret in credentials
//  5. create oidc IDP with the `issuer` field correctly set to value of `iss`
//  6. create test user in the test realm and link account in the test IdP with the value of `sub`
@Service
@Profile("keycloak")
public class KeycloakIdMService implements IdMService {
  private final Keycloak keycloak;
  private String realmName;

  public KeycloakIdMService(@Value("${idm.keycloak.realm}") String realmName,
            @Value("${idm.keycloak.url}") String url,
            @Value("${idm.keycloak.oauth.clientId}") String clientId,
            @Value("${idm.keycloak.oauth.clientSecret}") String clientSecret) {
    this.keycloak = KeycloakBuilder.builder()
                        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                        .serverUrl(url)
                        .realm(realmName)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .build();
    this.realmName = realmName;
  }

  @Override
  public String getUserIdByIdentifier(String issuer, String identifier) {
    System.out.println("getUserIdByIdentifier");

    // TODO should be surefire way to extract identity provider from jwt and find the user
    IdentityProviderRepresentation provider = keycloak.realm(this.realmName).identityProviders().findAll().stream()
                                             .filter(idp -> issuer.equals(idp.getConfig().get("issuer")))
                                                  .findFirst().orElse(null);
    if (provider == null) {
      // TODO handle nonexistent issuer (probably log, return null as user id)
      return null;
    }

    UserRepresentation user = keycloak
            .realm(this.realmName)
            .users()
            .search(null,null,null,null,null, provider.getAlias(), identifier, 0, 1, null,true )
                                  .getFirst();
    return user == null ? null : user.getId();
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
  public Map<Role, Set<Integer>> getRolesByUserId(String userId) {
    // TODO this probably has to be custom per IdM, Keycloak does not support in-group roles,
    //  so authz has to be done based on group membership (ADMIN can probably be retrieved from role)

    return Map.of();
  }

  @Override
  public String getAttribute(String attributeName, String userId, String groupId, String voId)
      throws IdmAttributeNotExistsException {
    // TODO rework into one attribute retrieval method

    // check that attribute exists
    RealmResource realmResource = keycloak.realm(this.realmName);
    if (
       realmResource.users().userProfile().getConfiguration().getAttribute(attributeName) == null
    ) {
      throw new IdmAttributeNotExistsException(attributeName);
    }

    // TODO how to handle multivalued attributes
    return realmResource.users().get(userId).toRepresentation().getAttributes().get(attributeName).getFirst();
  }

  @Override
  public String getLoginAttributeUrn() {
    return "";
  }

  @Override
  public boolean checkGroupExists(String groupId) {
    RealmResource realmResource = keycloak.realm(this.realmName);

    return realmResource.groups().group(groupId) != null;
  }

  @Override
  public boolean canExtendMembership(String userId, String groupId) {
    // TODO this has to be done using a custom attribute

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
