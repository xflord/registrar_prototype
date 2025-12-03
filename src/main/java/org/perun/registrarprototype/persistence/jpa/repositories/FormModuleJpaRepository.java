package org.perun.registrarprototype.persistence.jpa.repositories;

import java.util.List;

import org.perun.registrarprototype.persistence.jpa.entities.AssignedFormModuleJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("jpa")
public interface FormModuleJpaRepository extends JpaRepository<AssignedFormModuleJpaEntity,Integer> {
  List<AssignedFormModuleJpaEntity> findByFormId(Integer formId);
}
