package io.github.zlatozar.repair.workshop.events;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import org.jmolecules.event.annotation.DomainEvent;

/** The instrument cannot be repaired and will not come back. */
@DomainEvent
public record InstrumentWrittenOff(String job, InstrumentId instrument, String reason) {}
