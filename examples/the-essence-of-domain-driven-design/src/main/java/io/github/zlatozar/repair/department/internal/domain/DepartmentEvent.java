package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.List;
import org.jmolecules.event.annotation.DomainEvent;

/** What happened in a department. Only some of it is told to the other contexts. */
public sealed interface DepartmentEvent {

  @DomainEvent
  record RepairBoxApproved(
      DepartmentCode department,
      RepairBoxId box,
      RepairCategory category,
      List<RepairBox.Item> items)
      implements DepartmentEvent {

    public RepairBoxApproved {
      items = List.copyOf(items);
    }
  }

  @DomainEvent
  record ReplacementRequested(
      DepartmentCode department, InstrumentId instrument, Supplier supplier, Damage damage)
      implements DepartmentEvent {}
}
