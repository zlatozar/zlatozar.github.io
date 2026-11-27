package io.github.zlatozar.repair.accounting.internal.application.port.in;

import io.github.zlatozar.repair.accounting.internal.domain.Repair;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;

public record RecordRepairCommand(InstrumentId asset, Repair repair) {}
