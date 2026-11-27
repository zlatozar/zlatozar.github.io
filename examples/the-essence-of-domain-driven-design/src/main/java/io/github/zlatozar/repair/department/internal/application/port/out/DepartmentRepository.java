package io.github.zlatozar.repair.department.internal.application.port.out;

import io.github.zlatozar.repair.department.internal.domain.Department;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;

/** Loads and saves a whole department: the aggregate is the unit of persistence. */
@Repository
public interface DepartmentRepository {

  /** For reading only: takes no lock. */
  Optional<Department> findByCode(DepartmentCode code);

  /**
   * For a change: locks the department until the transaction ends. A save rewrites the whole
   * aggregate, so a second nurse waits, then loads the department with the first change in it. Both
   * changes are kept.
   */
  Optional<Department> findByCodeForUpdate(DepartmentCode code);

  void save(Department department);
}
