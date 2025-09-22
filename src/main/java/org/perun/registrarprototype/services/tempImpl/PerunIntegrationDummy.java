package org.perun.registrarprototype.services.tempImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.services.PerunIntegrationService;
import org.springframework.stereotype.Service;

// in-memory dummy implementation of Perun IDM integration
@Service
public class PerunIntegrationDummy implements PerunIntegrationService {
  private static final Map<Integer, List<Integer>> memberships = new HashMap<>();

  @Override
  public void registerUserToGroup(int userId, int groupId) {
    memberships.get(groupId).add(userId);
  }

  // test helper methods
  public void createGroup(int groupId) {
    memberships.put(groupId, new ArrayList<>());
  }

  public boolean isUserMemberOfGroup(int groupId, int userId) {
    return memberships.get(groupId).contains(userId);
  }
}
