package io.github.zlatozar.repair.workshop.events;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import org.jmolecules.event.annotation.DomainEvent;

/** The instrument is repaired, and the repair cost this much. */
@DomainEvent
public record InstrumentRepaired(String job, InstrumentId instrument, Money cost) {}
