package org.perun.registrarprototype.core.events;

public abstract class Event {

  protected String name = getClass().getSimpleName();

  public String getEventName() {
    return name;
  }

  /**
   * Get event message in predefined format.
   *
   * @return
   */
  public abstract String getEventMessage();
}
