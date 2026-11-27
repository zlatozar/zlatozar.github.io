package io.github.zlatozar.repair.workshop.internal.infrastructure.persistence;

import io.github.zlatozar.repair.workshop.internal.infrastructure.persistence.RepairJobRow.State;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.Repository;

/** Spring Data generates it. Only the reads the adapter needs; the writes are explicit. */
interface RepairJobTable extends Repository<RepairJobRow, String> {

  Optional<RepairJobRow> findById(String id);

  /** SELECT ... FOR UPDATE. Spring Data ignores the words between "find" and "By". */
  @Lock(LockMode.PESSIMISTIC_WRITE)
  Optional<RepairJobRow> findForUpdateById(String id);

  List<RepairJobRow> findByStateInOrderById(Collection<State> states);

  boolean existsById(String id);
}
