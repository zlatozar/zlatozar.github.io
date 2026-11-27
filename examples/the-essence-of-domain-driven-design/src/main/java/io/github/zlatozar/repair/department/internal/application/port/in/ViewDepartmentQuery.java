package io.github.zlatozar.repair.department.internal.application.port.in;

import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;

/** Shows a department's instruments, each with its state, and its open repair boxes. */
public interface ViewDepartmentQuery {

  DepartmentView view(DepartmentCode department);
}
