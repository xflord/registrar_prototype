package org.perun.registrarprototype.services.idmIntegration.perun;

import cz.metacentrum.perun.openapi.PerunRPC;
import cz.metacentrum.perun.openapi.model.ApplicationFormItem;
import cz.metacentrum.perun.openapi.model.ApplicationFormItemData;
import cz.metacentrum.perun.openapi.model.Attribute;
import cz.metacentrum.perun.openapi.model.AttributeDefinition;
import cz.metacentrum.perun.openapi.model.Candidate;
import cz.metacentrum.perun.openapi.model.EnrichedIdentity;
import cz.metacentrum.perun.openapi.model.ExtSource;
import cz.metacentrum.perun.openapi.model.Group;
import cz.metacentrum.perun.openapi.model.InputCheckForSimilarUsersWithData;
import cz.metacentrum.perun.openapi.model.InputCreateMemberForCandidate;
import cz.metacentrum.perun.openapi.model.InputCreateMemberForUser;
import cz.metacentrum.perun.openapi.model.InputSetMemberGroupWithUserAttributes;
import cz.metacentrum.perun.openapi.model.InputSetMemberWithUserAttributes;
import cz.metacentrum.perun.openapi.model.Member;
import cz.metacentrum.perun.openapi.model.Type;
import cz.metacentrum.perun.openapi.model.User;
import cz.metacentrum.perun.openapi.PerunException;
import cz.metacentrum.perun.openapi.model.UserExtSource;
import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.perun.registrarprototype.events.IdMUserCreatedEvent;
import org.perun.registrarprototype.events.MemberCreatedEvent;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.IdmAttributeNotExistsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.Role;
import org.perun.registrarprototype.services.EventService;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
public class PerunIdMService implements IdMService {
  private static final String DISPLAY_NAME = "urn:perun:user:attribute-def:core:displayName";
  private final List<String> GROUP_MANAGER_ROLES = List.of("GROUPADMIN", "GROUPMEMBERSHIPMANAGER");
  private final List<String> VO_MANAGER_ROLES = List.of("VOADMIN", "ORGANIZATIONMEMBERSHIPMANAGER");
  @Value( "${perun.einfra.ext-source}")
  private String idmExtSourceName = "test-ext-source";

  private final PerunRPC rpc;
  private final EventService eventService;


  public PerunIdMService(PerunRPC rpc, EventService eventService) {
    this.rpc = rpc;
    this.eventService = eventService;
  }

  @Override
  public Integer getUserIdByIdentifier(String identifier) {
    System.out.println("Calling getUserIdByIdentifier with parameter " + identifier);
    User user;
    try {
      user = rpc.getUsersManager().getUserByExtSourceNameAndExtLogin(identifier, idmExtSourceName);
    } catch (HttpClientErrorException ex) {
      // another way of handling this - logging and returning null?
      System.out.println(ex);
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

    if (user == null) {
      return null;
    }

    return user.getId();
  }

  @Override
  public List<Integer> getGroupIdsWhereUserIsMember(Integer userId) {
    // TODO update this on demand, currently retrieved with user roles

    return List.of();
  }

  @Override
  public Map<String, List<Integer>> getAuthorizedObjects(Integer userId) {
    Map<String, List<Integer>> objects = new HashMap<>();
    objects.put("Group", List.of());
    objects.put("VO", List.of());

    Map<String, Map<String, List<Integer>>> perunRoles;
    perunRoles = rpc.getAuthzResolver().getUserRoles(userId);


    for (String role : perunRoles.keySet()) {
      if (GROUP_MANAGER_ROLES.contains(role)) {
        objects.get("Group").addAll(perunRoles.get(role).get("Group"));
      }
      if (VO_MANAGER_ROLES.contains(role)) {
        objects.get("VO").addAll(perunRoles.get(role).get("Vo"));
      }
    }
    // TODO do we add all the groups for VO roles? Could be unnecessarily too many calls. Should be enough to save the
    //  ids and then fetch on demand (when checking permissions for group).

    return objects;
  }

  @Override
  public Map<Role, Set<Integer>> getRolesByUserId(Integer userId) {
    System.out.println("Calling getRegistrarRolesByUserId with parameter " + userId);
    Map<Role, Set<Integer>> regRoles = new HashMap<>();
    if (userId == null) {
      return regRoles;
    }
    regRoles.put(Role.FORM_MANAGER, new HashSet<>());
    regRoles.put(Role.FORM_APPROVER, new HashSet<>());
    regRoles.put(Role.MEMBERSHIP, new HashSet<>());

    Map<String, Map<String, List<Integer>>> perunRoles;
    perunRoles = rpc.getAuthzResolver().getUserRoles(userId);

    for (String role : perunRoles.keySet()) {
      switch (role) {
        case "VOADMIN": // handle rights for groups currently
          regRoles.get(Role.FORM_MANAGER).addAll(perunRoles.get(role).get("Vo").stream()
                                                     .flatMap(
                                                         voId -> rpc.getGroupsManager().getAllGroups(voId).stream())
                                                     .map(Group::getId).collect(Collectors.toSet()));
          break;
        case "ORGANIZATIONMEMBERSHIPMANAGER":
          regRoles.get(Role.FORM_APPROVER).addAll(perunRoles.get(role).get("Vo").stream()
                                                      .flatMap(
                                                          voId -> rpc.getGroupsManager().getAllGroups(voId).stream())
                                                      .map(Group::getId).collect(Collectors.toSet()));
          break;
        case "GROUPADMIN":
          regRoles.get(Role.FORM_MANAGER).addAll(perunRoles.get(role).get("Group"));
          break;
        case "GROUPMEMBERSHIPMANAGER":
          regRoles.get(Role.FORM_APPROVER).addAll(perunRoles.get(role).get("Group"));
          break;
        case "PERUNADMIN":
          regRoles.putIfAbsent(Role.ADMIN, Set.of());
          break;
        case "MEMBERSHIP":
          // TODO probably not ideal way to store membership (retrieve on demand)
          regRoles.get(Role.MEMBERSHIP).addAll(perunRoles.get(role).get("Group"));
        default:
          break;
      }
    }
    return regRoles;
  }

  @Override
  public String getUserAttribute(Integer userId, String attributeName) throws IdmAttributeNotExistsException {
    System.out.println("Calling getRegistrarRolesByUserId with parameter " + userId + " and attribute " + attributeName);

    if (userId == null) {
      return null;
    }

    try {
      Attribute attr = rpc.getAttributesManager().getUserAttributeByName(userId, attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("AttributeNotExistsException")) {
        // TODO log these (missing source attributes) as misconfigured forms
        throw new IdmAttributeNotExistsException(ex.getMessage(), attributeName);
      } else {
        throw ex;
      }
    }
  }

  @Override
  public String getMemberAttribute(Integer userId, String attributeName, Integer groupId)
      throws IdmAttributeNotExistsException {
    System.out.println("Calling getRegistrarRolesByUserId with parameter " + userId + " and attribute " + attributeName);

    if (userId == null) {
      return null;
    }

    Group group = retrieveGroup(groupId);
    if (group == null) {
      return null;
    }

    Member member = retrieveMember(userId, group);
    if (member == null) {
      return null;
    }

    try {
      Attribute attr = rpc.getAttributesManager().getMemberAttributeByName(member.getId(), attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("AttributeNotExistsException")) {
        // TODO log these (missing source attributes) as misconfigured forms
        throw new IdmAttributeNotExistsException(ex.getMessage(), attributeName);
      } else {
        throw ex;
      }
    }
  }

  @Override
  public String getMemberGroupAttribute(Integer userId, String attributeName, Integer groupId)
      throws IdmAttributeNotExistsException {
    if (userId == null) {
      return null;
    }

    Group group = retrieveGroup(groupId);
    if (group == null) {
      return null;
    }

    Member member = retrieveMember(userId, group);
    if (member == null) {
      return null;
    }

    try {
      Attribute attr = rpc.getAttributesManager().getMemberGroupAttributeByName(member.getId(), groupId, attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("AttributeNotExistsException")) {
        // TODO log these (missing source attributes) as misconfigured forms
        throw new IdmAttributeNotExistsException(ex.getMessage(), attributeName);
      } else {
        throw ex;
      }
    }
  }

  @Override
  public boolean checkGroupExists(Integer groupId) {
    try {
      rpc.getGroupsManager().getGroupById(groupId);
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("GroupNotExistsException")) {
        return false;
      } else {
        throw ex;
      }
    }
    return true;
  }

  private Member retrieveMember(Integer userId, Group group) {
    Member member;
    try {
      member = rpc.getMembersManager().getMemberByUser(group.getVoId(), userId);
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("MemberNotExistsException")) {
        return null;
      } else {
        throw ex;
      }
    }
    return member;
  }

  @Override
  public boolean canExtendMembership(Integer userId, Integer groupId) {
    if (userId == null) {
      return false;
    }
    Group group = retrieveGroup(groupId);

    Member member = retrieveMember(userId, group);
    if (member == null) {
      return false;
    }

    return rpc.getGroupsManager().canExtendMembershipInGroup(member.getId(), group.getId());
  }

  @Override
  public String getVoAttribute(String attributeName, int voId) throws IdmAttributeNotExistsException {
    try {
      Attribute attr = rpc.getAttributesManager().getVoAttributeByName(voId, attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("AttributeNotExistsException")) {
        throw new IdmAttributeNotExistsException(ex.getMessage(), attributeName);
      } else {
        throw ex;
      }
    }
  }

  @Override
  public String getGroupAttribute(String attributeName, Integer groupId) throws IdmAttributeNotExistsException {
    try {
      Attribute attr = rpc.getAttributesManager().getGroupAttributeByName(groupId, attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("AttributeNotExistsException")) {
        // TODO log these (missing source attributes) as misconfigured forms
        throw new IdmAttributeNotExistsException(ex.getMessage(), attributeName);
      } else {
        throw ex;
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition(String attributeName) {
    try {
      return rpc.getAttributesManager().getAttributeDefinitionByName(attributeName);
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("AttributeNotExistsException")) {
        return null;
      } else {
        throw ex;
      }
    }
  }

  @Override
  public boolean isLoginAvailable(String namespace, String login) {
    try {
      return rpc.getUsersManager().isLoginAvailable(namespace, login) == 1;
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("InvalidLoginException")) {
        // TODO throw custom exception? Exception includes reason why login couldn't be reserved -> invalid format or not allowed
        throw ex;
      } else {
        throw ex;
      }
    }
  }

  @Override
  public void reserveLogin(String namespace, String login) {
    // not implemented
  }

  @Override
  public void releaseLogin(String namespace, String login) {
    // not implemented
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
    return "urn:perun:user:attribute-def:def:login-namespace:";
  }

  @Override
  public String getUserAttributeUrn() {
    return "urn:perun:user";
  }

  @Override
  public String getMemberAttributeUrn() {
    return "urn:perun:member";
  }

  @Override
  public String getGroupAttributeUrn() {
    return "urn:perun:group";
  }

  @Override
  public String getVoAttributeUrn() {
    return "urn:perun:vo";
  }

  @Override
  public Integer createMemberForCandidate(Application application) {
    Group group = retrieveGroup(application.getForm().getGroupId());
    if (group == null) {
      return null;
    }

    InputCreateMemberForCandidate input = new InputCreateMemberForCandidate();
    input.setVo(group.getVoId());
    input.addGroupsItem(group);
    Candidate candidate = getCandidate(application);
    Map<String, String> attributes = new HashMap<>();
        application.getFormItemData().stream()
            .filter(item -> StringUtils.isNotEmpty(item.getFormItem().getItemDefinition().getDestinationAttributeUrn()))
            // TODO some necessary filtering/processing might be done here, see `createCandidateFromApplicationData` in Perun
            .forEach(item -> attributes.put(item.getFormItem().getItemDefinition().getDestinationAttributeUrn(), item.getValue()));
    candidate.setAttributes(attributes);
    input.setCandidate(candidate);
    Member createdMember = rpc.getMembersManager().createMemberForCandidate(input);

    rpc.getMembersManager().validateMemberAsync(createdMember.getId());

    eventService.emitEvent(new IdMUserCreatedEvent(createdMember.getUserId()));
    eventService.emitEvent(new MemberCreatedEvent(createdMember.getUserId(), group.getId(), createdMember.getId()));

    return createdMember.getUserId();
  }

  private Group retrieveGroup(int groupId) {
    Group group;
    try {
      group = rpc.getGroupsManager().getGroupById(groupId);
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("GroupNotExistsException")) {
        throw new DataInconsistencyException("Group with id " + groupId + " not found in Perun");
      } else {
        throw ex;
      }
    }

    return group;
  }

  @Override
  public Integer createMemberForUser(Application application) {
    Group group = retrieveGroup(application.getForm().getGroupId());
    if (group == null) {
      return null;
    }
    Member member;
    try {
      member = rpc.getMembersManager().getMemberByUser(group.getVoId(), application.getIdmUserId());
    } catch (PerunRuntimeException ex) {
      if (ex.getName().equals("MemberNotExistsException")) {
        member = null;
      } else {
        throw ex;
      }
    }

    if (member == null) {
      InputCreateMemberForUser input = new InputCreateMemberForUser();
      input.setVo(group.getVoId());
      input.addGroupsItem(group);
      input.setUser(application.getIdmUserId());
      member = rpc.getMembersManager().createMemberForUser(input);
      updateMemberAttributesFromAppData(application, member, group);

      rpc.getMembersManager().validateMemberAsync(member.getId());

      eventService.emitEvent(new MemberCreatedEvent(member.getUserId(), group.getId(), member.getId()));

      return member.getUserId();
    }

    // Already VO member
    List<Integer> members = new ArrayList<>();
    members.add(member.getId());
    rpc.getGroupsManager().addMembers(group.getId(), members);
    updateMemberAttributesFromAppData(application, member, group);

    return member.getUserId();
  }

  @Override
  public Integer extendMembership(Application application) {
    Group group = retrieveGroup(application.getForm().getGroupId());
    if (group == null) {
      return null;
    }

    Member member = rpc.getMembersManager().getMemberByUser(group.getVoId(), application.getIdmUserId());
    rpc.getMembersManager().extendMembership(member.getId());
    updateMemberAttributesFromAppData(application, member, group);

    return member.getUserId();
  }

  @Override
  public List<Identity> checkForSimilarUsers(String accessToken) {
    // hacky way to call Perun with user session
    RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    restTemplate.setErrorHandler(new RpcErrorHandler());
    PerunRPC tempRpc = new PerunRPC(restTemplate);
    tempRpc.getApiClient().setBasePath(rpc.getApiClient().getBasePath());
    tempRpc.getApiClient().setBearerToken(accessToken);

    List<EnrichedIdentity> perunIdentities = tempRpc.getRegistrarManager().checkForSimilarRichIdentities();

    return convertRichToDomainIdentities(perunIdentities);
  }

  @Override
  public List<Identity> checkForSimilarUsers(String accessToken, List<FormItemData> itemData) {
    // hacky way to call Perun with user session
    RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    restTemplate.setErrorHandler(new RpcErrorHandler());
    PerunRPC tempRpc = new PerunRPC(restTemplate);
    tempRpc.getApiClient().setBasePath(rpc.getApiClient().getBasePath());
    tempRpc.getApiClient().setBearerToken(accessToken);
    // TODO consider placing `FormItemData` in the external api package and importing it in perun (once old Registrar gets removed)
    List<ApplicationFormItemData> perunFormItems = new ArrayList<>();

    itemData.stream()
        .filter(item -> item.getFormItem().getItemDefinition().getDestinationAttributeUrn() != null ||
                            item.getFormItem().getItemDefinition().getType().equals(ItemType.VERIFIED_EMAIL))
        .map(item -> {
          ApplicationFormItemData perunAppData = new ApplicationFormItemData();
          perunAppData.setValue(item.getValue());
          ApplicationFormItem perunAppItem = new ApplicationFormItem();
          perunAppItem.setPerunDestinationAttribute(item.getFormItem().getItemDefinition().getDestinationAttributeUrn());
          if (item.getFormItem().getItemDefinition().getType().equals(ItemType.VERIFIED_EMAIL)) {
            perunAppItem.setType(Type.VALIDATED_EMAIL);
          }
          perunAppData.setFormItem(perunAppItem);
          return perunAppData;
        }).forEach(perunFormItems::add);

    InputCheckForSimilarUsersWithData input = new InputCheckForSimilarUsersWithData();
    input.setFormItems(perunFormItems);
    List<cz.metacentrum.perun.openapi.model.Identity> perunIdentities = tempRpc.getRegistrarManager().checkForSimilarUsersWithFormItemData(input);
    return convertToDomainIdentities(perunIdentities);
  }

  private List<Identity> convertRichToDomainIdentities(List<EnrichedIdentity> perunIdentities) {
    List<Identity> domainIdentities = new ArrayList<>();
    perunIdentities.forEach(identity -> {
      if (identity.getIdentities() == null) {
        // this should never be returned from Perun BE
        throw new IllegalStateException("Null identity array returned from Perun.");
      }
      identity.getIdentities().forEach(extSource -> {
        if (extSource.getExtSource() == null) {
        // this should never be returned from Perun BE
        throw new IllegalStateException("Null ExtSource in EnrichedExtSource returned from Perun.");
      }
        domainIdentities.add(new Identity(identity.getName(), identity.getOrganization(),
            identity.getEmail(), extSource.getExtSource().getType(), extSource.getAttributes()));
      });
    });
    return domainIdentities;
  }

  private List<Identity> convertToDomainIdentities(List<cz.metacentrum.perun.openapi.model.Identity> perunIdentities) {
    List<Identity> domainIdentities = new ArrayList<>();
    perunIdentities.forEach(identity -> {
      if (identity.getIdentities() == null) {
        // this should never be returned from Perun BE
        throw new IllegalStateException("Null identity array returned from Perun.");
      }
      identity.getIdentities().forEach(extSource -> {
        domainIdentities.add(new Identity(identity.getName(), identity.getOrganization(),
            identity.getEmail(), extSource.getType(), new HashMap<>()));
      });
    });
    return domainIdentities;
  }

  private void updateMemberAttributesFromAppData(Application application, Member member) {
    InputSetMemberWithUserAttributes inputAttributes = new InputSetMemberWithUserAttributes();
    inputAttributes.setMember(member.getId());
    List<Attribute> attributes = mapFormDataToAttributeObjects(application.getFormItemData());
    inputAttributes.setAttributes(attributes);
    inputAttributes.setWorkWithUserAttributes(true);
    rpc.getAttributesManager().setMemberWithUserAttributes(inputAttributes);
  }

  private void updateMemberAttributesFromAppData(Application application, Member member, Group group) {
    InputSetMemberGroupWithUserAttributes inputAttributes = new InputSetMemberGroupWithUserAttributes();
    inputAttributes.setMember(member.getId());
    inputAttributes.setGroup(group.getId());
    List<Attribute> attributes = mapFormDataToAttributeObjects(application.getFormItemData());
    inputAttributes.setAttributes(attributes);
    inputAttributes.setWorkWithUserAttributes(true);
    rpc.getAttributesManager().setMemberGroupWithUserAttributes(inputAttributes);
  }

  private List<Attribute> mapFormDataToAttributeObjects(List<FormItemData> itemData) {
    return itemData.stream()
               .filter(item -> item.getFormItem().getItemDefinition().getDestinationAttributeUrn() != null)
               .map(item -> {
                 AttributeDefinition attrDef = rpc.getAttributesManager()
                                                   .getAttributeDefinitionByName(item.getFormItem().getItemDefinition().getDestinationAttributeUrn());
                 Attribute attr = new Attribute();
                 attr.setId(attrDef.getId());
                 attr.setValue(item.getValue());
                 attr.setNamespace(attrDef.getNamespace());
                 attr.setType(attrDef.getType());
                 attr.setFriendlyName(attrDef.getFriendlyName());
                 return attr;
               })
               .toList();
  }

  private Candidate getCandidate(Application application) {
    Candidate candidate = new Candidate();
    // TODO what to do for unauthenticated users that do not consolidate and do not fill out any information (e.g no display name, given/last name in form)
    //  do not allow such forms, do not allow anonymous users?
    //  Old registrar currently uses `LOCAL` internal ExtSource with `createdBy` (e.g. timestamp) as login in such situations

    candidate.setFirstName(application.getSubmission().getIdentityAttributes().get("given_name"));
    candidate.setLastName(application.getSubmission().getIdentityAttributes().get("family_name"));

    String nameFromDisplayNameAttr = application.getFormItemData()
                                                        .stream()
                                                        .filter(formItemData -> formItemData.getFormItem().getItemDefinition().getDestinationAttributeUrn().equals(DISPLAY_NAME))
                                                        .map(FormItemData::getValue)
                                         .findFirst().orElse(null);
    NameParser.ParsedName parsedName = NameParser.parseDisplayName(nameFromDisplayNameAttr);

    if (parsedName != null) {
      // TODO do this even if names from IdP are provided?
      candidate.setTitleBefore(parsedName.titleBefore());
      candidate.setFirstName(parsedName.firstName());
      candidate.setMiddleName(parsedName.middleName());
      candidate.setLastName(parsedName.lastName());
      candidate.setTitleAfter(parsedName.titleAfter());
    }


    if (application.getSubmission().getSubmitterName() != null) {
      var ues = new UserExtSource();
      ues.setLoa(1);
      ues.setLogin(application.getSubmission().getIdentityIdentifier());
      var es = new ExtSource();
      es.setName(application.getSubmission().getIdentityIssuer());
      es.setType("cz.metacentrum.perun.core.impl.ExtSourceIdp");
      ues.setExtSource(es);
      candidate.setUserExtSource(ues);
    }
    return candidate;
  }
}
