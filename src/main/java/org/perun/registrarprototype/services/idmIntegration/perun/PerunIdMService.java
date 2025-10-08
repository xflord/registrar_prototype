package org.perun.registrarprototype.services.idmIntegration.perun;

import cz.metacentrum.perun.openapi.PerunRPC;
import cz.metacentrum.perun.openapi.model.Attribute;
import cz.metacentrum.perun.openapi.model.AttributeDefinition;
import cz.metacentrum.perun.openapi.model.Group;
import cz.metacentrum.perun.openapi.model.Identity;
import cz.metacentrum.perun.openapi.model.Member;
import cz.metacentrum.perun.openapi.model.User;
import cz.metacentrum.perun.openapi.PerunException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;


@Service
public class PerunIdMService implements IdMService {
  private final List<String> GROUP_MANAGER_ROLES = List.of("GROUPADMIN", "GROUPMEMBERSHIPMANAGER");
  private final List<String> VO_MANAGER_ROLES = List.of("VOADMIN", "ORGANIZATIONMEMBERSHIPMANAGER");
  @Value( "${perun.einfra.ext-source}")
  private String idmExtSourceName = "test-ext-source";

  private final PerunRPC rpc;

  public PerunIdMService(PerunRPC rpc) {
    this.rpc = rpc;
  }

  @Override
  public User getUserByIdentifier(String identifier) {
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

    return user;
  }

  @Override
  public List<Integer> getGroupIdsWhereUserIsMember(String identifier) {

    return List.of();
  }

  @Override
  public Map<String, List<Integer>> getAuthorizedObjects(String identifier) throws Exception {
    User user = getUser(identifier);

    if (user == null) {
      return new HashMap<>();
    }

    Map<String, List<Integer>> objects = new HashMap<>();
    objects.put("Group", List.of());
    objects.put("VO", List.of());

    Map<String, Map<String, List<Integer>>> perunRoles;
    try {
      perunRoles = rpc.getAuthzResolver().getUserRoles(user.getId());
    } catch (HttpClientErrorException ex) {
      throw PerunException.to(ex);
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

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
  public String getUserAttribute(String identifier, String attributeName) {
    User user = getUser(identifier);

    if (user == null) {
      return null;
    }

    try {
      Attribute attr = rpc.getAttributesManager().getUserAttributeByName(user.getId(), attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (HttpClientErrorException ex) {
      // another way of handling this - logging and returning null?
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getMemberAttribute(String identifier, String attributeName, int groupId) {
    User user = getUser(identifier);

    if (user == null) {
      return null;
    }

    Group group;
    try {
      group = rpc.getGroupsManager().getGroupById(groupId);
    } catch (HttpClientErrorException ex) {
      // another way of handling this - logging and returning null?
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

    Member member;
    try {
      member = rpc.getMembersManager().getMemberByUser(group.getVoId(), user.getId());
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

    try {
      Attribute attr = rpc.getAttributesManager().getMemberAttributeByName(member.getId(), attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getMemberGroupAttribute(String identifier, String attributeName, int groupId) {
    User user = getUser(identifier);

    if (user == null) {
      return null;
    }

    Group group;
    try {
      group = rpc.getGroupsManager().getGroupById(groupId);
    } catch (HttpClientErrorException ex) {
      // another way of handling this - logging and returning null?
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

    Member member;
    try {
      member = rpc.getMembersManager().getMemberByUser(group.getVoId(), user.getId());
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

    try {
      Attribute attr = rpc.getAttributesManager().getMemberGroupAttributeByName(member.getId(), groupId, attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean canExtendMembership(String identifier, int groupId) {
    User user = getUser(identifier);

    if (user == null) {
      return false;
    }

    Group group;
    try {
      group = rpc.getGroupsManager().getGroupById(groupId);
    } catch (HttpClientErrorException ex) {
      // another way of handling this - logging and returning null?
      System.out.println(PerunException.to(ex).getMessage());
      return false;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

    Member member;
    try {
      member = rpc.getMembersManager().getMemberByUser(group.getVoId(), user.getId());
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return false;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }

    try {
      return rpc.getGroupsManager().canExtendMembershipInGroup(member.getId(), group.getId());
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return false;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getVoAttribute(String attributeName, int voId) {
    try {
      Attribute attr = rpc.getAttributesManager().getVoAttributeByName(voId, attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getGroupAttribute(String attributeName, int groupId) {
    try {
      Attribute attr = rpc.getAttributesManager().getGroupAttributeByName(groupId, attributeName);
      return attr.getValue() == null ? null : attr.getValue().toString();
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition(String attributeName) {
    try {
      return rpc.getAttributesManager().getAttributeDefinitionByName(attributeName);
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return null;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean isLoginAvailable(String namespace, String login) {
    try {
      return rpc.getUsersManager().isLoginAvailable(namespace, login) == 1;
    } catch (HttpClientErrorException ex) {
      System.out.println(PerunException.to(ex).getMessage());
      return false;
    } catch (RestClientException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void reserveLogin(String namespace, String login) {
    // not implemented
  }

  @Override
  public List<Identity> getSimilarUsers(Map<String, Object> attributes) {
    return List.of();
  }

  private User getUser(String identifier) {
    User user;

    user = this.getUserByIdentifier(identifier);

    return user;
  }
}
