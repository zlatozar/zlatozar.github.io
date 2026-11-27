package io.github.zlatozar.repair.department.internal.application.service;

import io.github.zlatozar.repair.department.internal.application.port.in.ApproveRepairBoxCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.ApproveRepairBoxUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.DepartmentView;
import io.github.zlatozar.repair.department.internal.application.port.in.ReturnFromRepairCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.ReturnFromRepairUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.SendForRepairCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.SendForRepairUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.ViewDepartmentQuery;
import io.github.zlatozar.repair.department.internal.application.port.out.DepartmentEventPublisher;
import io.github.zlatozar.repair.department.internal.application.port.out.DepartmentRepository;
import io.github.zlatozar.repair.department.internal.domain.Department;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.UnknownDepartment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * One transaction per command, and one aggregate per transaction. Package-private: callers know
 * only the use cases, never this class.
 */
@Service
@Transactional
class DepartmentService
    implements SendForRepairUseCase,
        ApproveRepairBoxUseCase,
        ReturnFromRepairUseCase,
        ViewDepartmentQuery {

  private final DepartmentRepository departments;
  private final DepartmentEventPublisher events;

  DepartmentService(DepartmentRepository departments, DepartmentEventPublisher events) {
    this.departments = departments;
    this.events = events;
  }

  @Override
  public void sendForRepair(SendForRepairCommand command) {
    Department department = load(command.department());
    department.sendForRepair(command.instrument(), command.damage());
    save(department);
  }

  @Override
  public void approveRepairBox(ApproveRepairBoxCommand command) {
    Department department = load(command.department());
    department.approveRepairBox(command.box());
    save(department);
  }

  @Override
  public void returnFromRepair(ReturnFromRepairCommand command) {
    Department department = load(command.department());
    department.returnFromRepair(command.instrument());
    save(department);
  }

  @Override
  @Transactional(readOnly = true)
  public DepartmentView view(DepartmentCode code) {
    return departments
        .findByCode(code)
        .map(DepartmentView::of)
        .orElseThrow(() -> new UnknownDepartment(code));
  }

  private Department load(DepartmentCode code) {
    return departments.findByCodeForUpdate(code).orElseThrow(() -> new UnknownDepartment(code));
  }

  /** Saves first: when saving fails, no event leaves the department. */
  private void save(Department department) {
    departments.save(department);
    department.releaseEvents().forEach(events::publish);
  }
}
