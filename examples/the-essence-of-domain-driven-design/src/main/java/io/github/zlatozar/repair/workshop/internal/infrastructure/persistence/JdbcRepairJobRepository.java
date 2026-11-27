package io.github.zlatozar.repair.workshop.internal.infrastructure.persistence;

import io.github.zlatozar.repair.workshop.internal.application.port.out.RepairJobRepository;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJob;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import io.github.zlatozar.repair.workshop.internal.infrastructure.persistence.RepairJobRow.State;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Component;

/**
 * The driven adapter: turns the domain into rows and back. The ID comes from the domain, not from
 * the database, so Spring Data cannot tell a new job from a changed one: we insert and update
 * explicitly.
 */
@Component
class JdbcRepairJobRepository implements RepairJobRepository {

  private static final EnumSet<State> OPEN = EnumSet.of(State.RECEIVED, State.IN_REPAIR);

  private final RepairJobTable table;
  private final JdbcAggregateOperations rows;

  JdbcRepairJobRepository(RepairJobTable table, JdbcAggregateOperations rows) {
    this.table = table;
    this.rows = rows;
  }

  @Override
  public Optional<RepairJob> findById(RepairJobId id) {
    return table.findById(id.value()).map(RepairJobRow::toDomain);
  }

  @Override
  public Optional<RepairJob> findByIdForUpdate(RepairJobId id) {
    return table.findForUpdateById(id.value()).map(RepairJobRow::toDomain);
  }

  @Override
  public List<RepairJob> findOpen() {
    return table.findByStateInOrderById(OPEN).stream().map(RepairJobRow::toDomain).toList();
  }

  @Override
  public boolean exists(RepairJobId id) {
    return table.existsById(id.value());
  }

  @Override
  public void add(RepairJob job) {
    rows.insert(RepairJobRow.of(job));
  }

  @Override
  public void save(RepairJob job) {
    rows.update(RepairJobRow.of(job));
  }
}
