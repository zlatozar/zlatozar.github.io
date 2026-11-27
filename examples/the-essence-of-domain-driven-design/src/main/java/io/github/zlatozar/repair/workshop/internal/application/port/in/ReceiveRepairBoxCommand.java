package io.github.zlatozar.repair.workshop.internal.application.port.in;

import io.github.zlatozar.repair.workshop.internal.domain.RepairBox;

public record ReceiveRepairBoxCommand(RepairBox box) {}
