package io.github.zlatozar.repair.workshop.internal.application.port.in;

import io.github.zlatozar.repair.workshop.internal.domain.Outcome;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;

public record RecordOutcomeCommand(RepairJobId job, Outcome outcome) {}
