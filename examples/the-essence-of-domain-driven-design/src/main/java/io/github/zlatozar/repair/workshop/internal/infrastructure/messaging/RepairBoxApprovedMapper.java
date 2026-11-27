package io.github.zlatozar.repair.workshop.internal.infrastructure.messaging;

import io.github.zlatozar.repair.department.events.RepairBoxApproved;
import io.github.zlatozar.repair.workshop.internal.domain.RepairBox;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Turns the department's language into the workshop's. Enums map by name, so a new kind of damage
 * in the department fails this build, not the workshop at runtime.
 */
@Mapper
interface RepairBoxApprovedMapper {

  // The workshop does not care how the department sorts its boxes
  @BeanMapping(ignoreUnmappedSourceProperties = "category")
  @Mapping(target = "number", source = "box")
  RepairBox toRepairBox(RepairBoxApproved event);
}
