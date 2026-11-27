package io.github.zlatozar.repair.workshop.internal.application.port.out;

import io.github.zlatozar.repair.workshop.internal.domain.RepairJob;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;

/** Loads and stores repair jobs. Each job is its own aggregate: it is loaded and locked alone. */
@Repository
public interface RepairJobRepository {

  /** For reading only: takes no lock. */
  Optional<RepairJob> findById(RepairJobId id);

  /** For a change: locks the job until the transaction ends. */
  Optional<RepairJob> findByIdForUpdate(RepairJobId id);

  /** For reading only: the jobs that are not finished, sorted by ID. */
  List<RepairJob> findOpen();

  boolean exists(RepairJobId id);

  /** Stores a new job. */
  void add(RepairJob job);

  /** Stores the changes of a job loaded for update. */
  void save(RepairJob job);
}
