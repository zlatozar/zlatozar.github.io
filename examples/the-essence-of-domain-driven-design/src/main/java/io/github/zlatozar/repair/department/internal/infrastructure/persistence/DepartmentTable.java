package io.github.zlatozar.repair.department.internal.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.Repository;

/** Spring Data generates it. Only the methods the adapter needs, nothing more. */
interface DepartmentTable extends Repository<DepartmentRow, String> {

  Optional<DepartmentRow> findByCode(String code);

  /** SELECT ... FOR UPDATE. Spring Data ignores the words between "find" and "By". */
  @Lock(LockMode.PESSIMISTIC_WRITE)
  Optional<DepartmentRow> findForUpdateByCode(String code);

  DepartmentRow save(DepartmentRow department);
}
