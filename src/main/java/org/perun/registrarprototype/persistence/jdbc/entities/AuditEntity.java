package org.perun.registrarprototype.persistence.jdbc.entities;

import java.time.Instant;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

public abstract class AuditEntity {

  @CreatedDate
  private Instant createdAt;
  @CreatedBy
  private String createdBy;
  @LastModifiedDate
  private Instant modifiedAt;
  @LastModifiedBy
  private String modifiedBy;
}
