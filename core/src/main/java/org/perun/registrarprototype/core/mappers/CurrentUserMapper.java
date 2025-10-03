package org.perun.registrarprototype.core.mappers;

import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.core.security.CurrentUser;
import org.perun.registrarprototype.extension.dto.CurrentUserDto;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserMapper {
  public CurrentUserDto toDto(CurrentUser sess) {
    CurrentUserDto dto = new CurrentUserDto();
    dto.setId(sess.id());
    dto.setAttributes(Map.copyOf(sess.getAttributes()));
    dto.setAuthenticated(sess.isAuthenticated());
    dto.setManagedGroups(Set.copyOf(dto.getManagedGroups()));

    return dto;
  }
}
