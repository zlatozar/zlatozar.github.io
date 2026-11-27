package io.github.zlatozar.repair.department.internal.infrastructure.messaging;

import io.github.zlatozar.repair.department.events.RepairBoxApproved;
import io.github.zlatozar.repair.department.events.ReplacementRequested;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct writes the code. The build fails when a domain event gets a field that is not mapped
 * here: someone must decide whether other modules should see it.
 */
@Mapper
interface DepartmentEventMapper {

  @Mapping(target = "department", source = "department.value")
  @Mapping(target = "box", source = "box.number")
  RepairBoxApproved toPublic(DepartmentEvent.RepairBoxApproved event);

  @Mapping(target = "department", source = "department.value")
  @Mapping(target = "supplier", source = "supplier.name")
  ReplacementRequested toPublic(DepartmentEvent.ReplacementRequested event);
}
