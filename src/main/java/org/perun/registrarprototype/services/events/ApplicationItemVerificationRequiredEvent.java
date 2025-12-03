package org.perun.registrarprototype.services.events;

import java.util.List;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;

public class ApplicationItemVerificationRequiredEvent extends ApplicationRelatedEvent {

  private List<FormItemData> itemsToVerify;

  public ApplicationItemVerificationRequiredEvent(Application application, List<FormItemData> itemToVerify) {
    super(application);
    this.itemsToVerify = itemToVerify;
  }

  @Override
  public String getEventMessage() {
    return this.formatEventMessage(application, itemsToVerify);
  }

  public List<FormItemData> getItemsToVerify() {
    return itemsToVerify;
  }
}
