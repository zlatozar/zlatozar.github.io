package io.github.zlatozar.repair.department.internal.application.port.in;

import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.RepairBoxId;

public record ApproveRepairBoxCommand(DepartmentCode department, RepairBoxId box) {}
