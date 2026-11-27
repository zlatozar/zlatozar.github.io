package io.github.zlatozar.repair.department.events;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import org.jmolecules.event.annotation.DomainEvent;

/** A third-party instrument is broken: its supplier must send a new one. */
@DomainEvent
public record ReplacementRequested(
    String department, InstrumentId instrument, String supplier, Damage damage) {}
