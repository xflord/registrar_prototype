package org.perun.registrarprototype.persistence.jdbc.entities;

import java.time.Instant;
import java.util.Map;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "audit_log")
public class AuditEventEntity {
  @Id
  private Integer id;

  private String eventName;

  private Instant timestamp;

  @Column("content")
  private RegistrarEvent content;

  @Column("actor")
  private CurrentUser actor;

  private String correlationId;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public RegistrarEvent getContent() {
    return content;
  }

  public void setContent(RegistrarEvent content) {
    this.content = content;
  }

  public CurrentUser getActor() {
    return actor;
  }

  public void setActor(CurrentUser actor) {
    this.actor = actor;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

}