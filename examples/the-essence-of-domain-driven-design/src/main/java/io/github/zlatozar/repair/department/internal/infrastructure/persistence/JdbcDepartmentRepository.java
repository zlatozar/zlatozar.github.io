package io.github.zlatozar.repair.department.internal.infrastructure.persistence;

import io.github.zlatozar.repair.department.internal.application.port.out.DepartmentRepository;
import io.github.zlatozar.repair.department.internal.domain.Department;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** The driven adapter: turns the domain into rows and back. */
@Component
class JdbcDepartmentRepository implements DepartmentRepository {

  private final DepartmentTable table;

  JdbcDepartmentRepository(DepartmentTable table) {
    this.table = table;
  }

  @Override
  public Optional<Department> findByCode(DepartmentCode code) {
    return table.findByCode(code.value()).map(DepartmentRow::toDomain);
  }

  @Override
  public Optional<Department> findByCodeForUpdate(DepartmentCode code) {
    return table.findForUpdateByCode(code.value()).map(DepartmentRow::toDomain);
  }

  @Override
  public void save(Department department) {
    table.save(DepartmentRow.of(department));
  }
}
