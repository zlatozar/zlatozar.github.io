package io.github.zlatozar.repair.workshop.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import org.jmolecules.event.annotation.DomainEvent;

/** What happened in the workshop. */
public sealed interface WorkshopEvent {

  @DomainEvent
  record InstrumentRepaired(RepairJobId job, InstrumentId instrument, Money cost)
      implements WorkshopEvent {}

  @DomainEvent
  record InstrumentWrittenOff(RepairJobId job, InstrumentId instrument, String reason)
      implements WorkshopEvent {}
}
