package org.perun.registrarprototype.services.idmIntegration.keycloak;

import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.ItemType;
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
//  7. create the required test attribute definitions
// TODO this is nowhere near production ready! Serves solely as proof of concept, plenty of implementation details to sort out
// tested: prefilling attributes, propagating attributes, creating user+adding to user, adding existing user to new group
@Service
@Profile("keycloak")
public class KeycloakIdMService implements IdMService {
  private final Keycloak keycloak;
  private final String expirationAttribute = "group_expirations";
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

    List<UserRepresentation> foundUsers = keycloak
            .realm(this.realmName)
            .users()
            .search(null,null,null,null,null, provider.getAlias(), identifier, 0, 1, null,true );
    if (foundUsers.isEmpty()) {
      return null;
    }
    if (foundUsers.size() > 1) {
      throw new IllegalStateException("More than one user found with identifier: " + identifier);
    }
    UserRepresentation user = foundUsers.getFirst();
    return user.getId();
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
    // TODO this probably has to be custom per IdM, Keycloak does not support in-group roles,
    //  so authz has to be done based on group membership (ADMIN can probably be retrieved from role)

    System.out.println("Calling getRegistrarRolesByUserId with parameter " + userId);
    Map<Role, Set<String>> regRoles = new HashMap<>();
    if (userId == null) {
      return regRoles;
    }

    List<GroupRepresentation> groups = keycloak.realm(this.realmName).users().get(userId).groups();
    System.out.println("getRegistrarRolesByUserId with groups " + groups);

    regRoles.put(Role.FORM_MANAGER, new HashSet<>());
    regRoles.put(Role.FORM_APPROVER, new HashSet<>());
    regRoles.put(Role.MEMBERSHIP, new HashSet<>(groups.stream().map(GroupRepresentation::getId).toList()));

    // TODO set as admin for now
    regRoles.putIfAbsent(Role.ADMIN, Set.of());
    return regRoles;
  }

  @Override
  public String getAttribute(String attributeName, String userId, String groupId, String voId)
      throws IdmAttributeNotExistsException {
    if (userId == null) {
      return null;
    }

    // check that attribute exists
    RealmResource realmResource = keycloak.realm(this.realmName);
    if (
       realmResource.users().userProfile().getConfiguration().getAttribute(attributeName) == null
    ) {
      throw new IdmAttributeNotExistsException(attributeName);
    }

    // TODO how to handle multivalued attributes
//    return realmResource.users().get(userId).toRepresentation().getAttributes().get(attributeName).getFirst();
    System.out.println(attributeName);
    UserResource userResource = realmResource.users().get(userId);
    if (userResource == null) {
      // TODO once we have userId we should consider that user always exists?
      return null;
    }
    List<String> attr = userResource.toRepresentation().getAttributes().get(attributeName);
    if (attr == null) {
      return null;
    }
    return String.join(", ", realmResource.users().get(userId).toRepresentation().getAttributes().get(attributeName));
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
    // TODO this has to be done using a custom attribute/logic

    UserRepresentation user = keycloak.realm(this.realmName).users().get(userId).toRepresentation();

    List<String> expires = user.getAttributes().get(expirationAttribute);
    if (expires == null || expires.isEmpty()) {
      return false;
    }

    String expiration = expires.stream()
        .filter(exp -> {
          String[] parts = exp.split(":");
          String id = parts[0];
          return id.equals(groupId);
        })
        .findFirst()
        .orElse(null);

    if (expiration == null) {
      return false;
    }

    long epoch = Long.parseLong(expiration.split(":")[1]);
    // TODO test implementation
    return Instant.now().isBefore(Instant.ofEpochMilli(epoch));
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

    UserRepresentation user = getUserRepresentation(application);

    Map<String, List<String>> attributes = new HashMap<>();

    application.getFormItemData().stream()
        .filter(item -> Objects.nonNull(item.getFormItem().getItemDefinition().getDestination()))
        // TODO some necessary filtering/processing might be done here, see `createCandidateFromApplicationData` in Perun
        .forEach(item -> {
          String attributeName = item.getFormItem().getItemDefinition().getDestination().getUrn();
          UPAttribute attrDef = keycloak.realm(this.realmName).users().userProfile().getConfiguration().getAttribute(attributeName);
          if (attrDef == null) {
            throw new IllegalStateException("Attribute " + attributeName + " not found in underlying IdM");
          }
          if (attributes.containsKey(attributeName)) {
            // TODO is this how we want to handle multiple same destination fields?
            if (attrDef.isMultivalued()) {
              attributes.get(attributeName).add(item.getValue());
            } else {
              attributes.put(attributeName, new ArrayList<>(List.of(item.getValue())));
            }
          } else {
            attributes.put(attributeName, new ArrayList<>(List.of(item.getValue())));
          }
        });

    List<String> expires = new ArrayList<>();
    expires.add(application.getFormSpecification().getGroupId() + ":" + Instant.now().plus(Duration.ofDays(30)).toEpochMilli());
    attributes.put(expirationAttribute, expires);

    user.setAttributes(attributes);

    String userId;
    try (Response response = keycloak.realm(this.realmName).users().create(user)) {
      if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
        userId = CreatedResponseUtil.getCreatedId(response);
      } else {
        System.out.println(response.readEntity(String.class));
        throw new RuntimeException("Unable to create user " + user.getUsername() + ", call failed with status " + response.getStatus());
      }
    }

    UserResource userResource = keycloak.realm(this.realmName).users().get(userId);
    userResource.joinGroup(application.getFormSpecification().getGroupId());


    //    if (application.getSubmission().getSubmitterName() != null) {
    //      // TODO not sure this is okay, since we're joining identities without alerting the user
    //      String issuer = application.getSubmission().getIdentityIssuer();
    //      String sub = application.getSubmission().getIdentityIdentifier();
    //      // TODO should be surefire way to extract identity provider from jwt and find the user
    //      IdentityProviderRepresentation provider = keycloak.realm(this.realmName).identityProviders().findAll().stream()
    //                                               .filter(idp -> issuer.equals(idp.getConfig().get("issuer")))
    //                                                    .findFirst().orElse(null);
    //      if (provider == null) {
    //        // TODO handle nonexistent issuer (probably log, return null as user id)
    //        return userId;
    //      }
    //      FederatedIdentityRepresentation federatedIdentityRepresentation = new FederatedIdentityRepresentation();
    //      federatedIdentityRepresentation.setIdentityProvider(provider.getAlias());
    //      federatedIdentityRepresentation.setUserId(sub);
    //      federatedIdentityRepresentation.setUserName(sub);
    //      try (Response response = userResource.addFederatedIdentity(provider.getAlias(), federatedIdentityRepresentation)) {
    //        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
    //          // theoretically keycloak should return 409 on conflict, e.g. on existing user with this linked identity
    //          throw new RuntimeException("Unable to create federated identity `" + issuer + ": " + sub + "` for user " + user.getUsername() + ", call failed with status " + response.getStatus());
    //        }
    //      }
    //    }


    return userId;
  }

  @Override
  public String createMemberForUser(Application application) {

    UserResource userResource = keycloak.realm(this.realmName).users().get(application.getIdmUserId());
    // join group
    userResource.joinGroup(application.getFormSpecification().getGroupId());
    // TODO joining group can inherit attributes in keycloak, right? Do we join group and overwrite attributes, or the other way around?
    UserRepresentation user = userResource.toRepresentation();
    Map<String, List<String>> attributes = user.getAttributes();

    // once again, how to handle single/multivalued attributes
    application.getFormItemData().stream()
        .filter(item -> Objects.nonNull(item.getFormItem().getItemDefinition().getDestination()))
        // TODO some necessary filtering/processing might be done here, see `createCandidateFromApplicationData` in Perun
        .forEach(item -> {
          String attributeName = item.getFormItem().getItemDefinition().getDestination().getUrn();
          UPAttribute attrDef = keycloak.realm(this.realmName).users().userProfile().getConfiguration().getAttribute(attributeName);
          if (attrDef == null) {
            throw new IllegalStateException("Attribute " + attributeName + " not found in underlying IdM");
          }
          if (attributes.containsKey(attributeName)) {
            // TODO is this how we want to handle multiple same destination fields?
            if (attrDef.isMultivalued()) {
              attributes.get(attributeName).add(item.getValue());
            } else {
              attributes.put(attributeName, new ArrayList<>(List.of(item.getValue())));
            }
          } else {
            attributes.put(attributeName, new ArrayList<>(List.of(item.getValue())));
          }
        });

    // TODO update the custom membership attribute (ideally a custom event listener inside Keycloak sets this)
    List<String> expires = user.getAttributes().getOrDefault(expirationAttribute, new ArrayList<>());
    expires.add(application.getFormSpecification().getGroupId() + ":" + Instant.now().plus(Duration.ofDays(30)).toEpochMilli());
    user.getAttributes().put(expirationAttribute, expires);

    user.setAttributes(attributes);

    userResource.update(user);

    return user.getId();
  }

  @Override
  public String extendMembership(Application application) {
    UserResource userResource = keycloak.realm(this.realmName).users().get(application.getIdmUserId());
     UserRepresentation user = userResource.toRepresentation();
    Map<String, List<String>> attributes = user.getAttributes();

    // once again, how to handle single/multivalued attributes
    application.getFormItemData().stream()
        .filter(item -> Objects.nonNull(item.getFormItem().getItemDefinition().getDestination()))
        // TODO some necessary filtering/processing might be done here, see `createCandidateFromApplicationData` in Perun
        .forEach(item -> {
          String attributeName = item.getFormItem().getItemDefinition().getDestination().getUrn();
          UPAttribute attrDef = keycloak.realm(this.realmName).users().userProfile().getConfiguration().getAttribute(attributeName);
          if (attrDef == null) {
            throw new IllegalStateException("Attribute " + attributeName + " not found in underlying IdM");
          }
          if (attributes.containsKey(attributeName)) {
            // TODO is this how we want to handle multiple same destination fields?
            if (attrDef.isMultivalued()) {
              attributes.get(attributeName).add(item.getValue());
            } else {
              attributes.put(attributeName, new ArrayList<>(List.of(item.getValue())));
            }
          } else {
            attributes.put(attributeName, new ArrayList<>(List.of(item.getValue())));
          }
        });

    List<String> expires = user.getAttributes().getOrDefault(expirationAttribute, new ArrayList<>());
    expires.add(application.getFormSpecification().getGroupId() + ":" + Instant.now().plus(Duration.ofDays(30)).toEpochMilli());
    user.getAttributes().put(expirationAttribute, expires);

    user.setAttributes(attributes);

    userResource.update(user);

    return user.getId();
  }

  @Override
  public List<Identity> checkForSimilarUsers(String accessToken) {
    return List.of();
  }

  @Override
  public List<Identity> checkForSimilarUsers(String accessToken, List<FormItemData> itemData) {
    return List.of();
  }

  private UserRepresentation getUserRepresentation(Application application) {
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);

    // setting the `firstName`, `lastName` attributes, etc. is probably also ok?
    user.setFirstName(application.getSubmission().getIdentityAttributes().get("given_name"));
    user.setLastName(application.getSubmission().getIdentityAttributes().get("family_name"));
    // TODO retrieve this from attributes as well

    application.getFormItemData().forEach(itemData -> {
      if (itemData.getFormItem().getItemDefinition().getType().equals(ItemType.VERIFIED_EMAIL)) {
        user.setEmail(itemData.getValue());
        user.setEmailVerified(true);
      }
      if ("username".equals(itemData.getFormItem().getItemDefinition().getDestination())) {
        // TODO could also use `login` item type (or define new), not sure if there are namespaces in Keycloak
        user.setUsername(itemData.getValue());
      }
    });

    if (user.getUsername() == null || user.getUsername().isEmpty()) {
      // TODO this is purely for testing
      user.setUsername(String.valueOf(Instant.now().getEpochSecond()));
    }

    return user;
  }
}
