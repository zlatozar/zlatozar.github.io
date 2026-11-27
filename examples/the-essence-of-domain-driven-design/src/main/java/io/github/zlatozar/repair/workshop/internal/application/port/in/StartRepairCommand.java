package io.github.zlatozar.repair.workshop.internal.application.port.in;

import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;

public record StartRepairCommand(RepairJobId job) {}
