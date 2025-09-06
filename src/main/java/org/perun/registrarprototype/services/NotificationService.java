package org.perun.registrarprototype.services;

public interface NotificationService {
  void notifyApplicationSubmitted();

  void notifyApplicationApproved();

  void notifyApplicationRejected();
}
