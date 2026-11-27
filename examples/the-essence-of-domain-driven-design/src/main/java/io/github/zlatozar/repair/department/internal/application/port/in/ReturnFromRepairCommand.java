package io.github.zlatozar.repair.department.internal.application.port.in;

import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;

public record ReturnFromRepairCommand(DepartmentCode department, InstrumentId instrument) {}
