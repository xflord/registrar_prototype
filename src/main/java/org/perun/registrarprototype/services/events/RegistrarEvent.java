package org.perun.registrarprototype.services.events;

import java.util.Collection;
import java.util.Map;

public abstract class RegistrarEvent {

  protected boolean auditable = false;
  protected boolean notifiable = false;

  protected String name = getClass().getSimpleName();

  public String getEventName() {
    return name;
  }

  public String getEventMessage() {
    return this.formatEventMessage();
  }

  /**
   * Get event message in predefined format.
   *
   * @return
   */
  protected String formatEventMessage(Object... args) {
    StringBuilder sb = new StringBuilder();
    sb.append(getEventName()).append(" - ");

    for (Object arg : args) {
      sb.append(formatObject(arg)).append(" ");
    }

    return sb.toString();
  }

  /**
   * Test parsing method with some random format, modify in the future based on decided format of auditing/messaging
   * @param obj
   * @return
   */
  private String formatObject(Object obj) {
    if (obj instanceof Collection<?> c) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (Object o : c) {
        sb.append(formatObject(o));
        sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
    }

    if (obj instanceof Map<?, ?> m) {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      for (Map.Entry<?, ?> e : m.entrySet()) {
        sb.append(formatObject(e.getKey()));
        sb.append(": ");
        sb.append(formatObject(e.getValue()));
        sb.append(", ");
      }
      sb.append("}");
      return sb.toString();
    }

    return obj == null ? "0x" : obj.toString();
  }

  public boolean isAuditable() {
    return auditable;
  }

  public boolean isNotifiable() {
    return notifiable;
  }
}
