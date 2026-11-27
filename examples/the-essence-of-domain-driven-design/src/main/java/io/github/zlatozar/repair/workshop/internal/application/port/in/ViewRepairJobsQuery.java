package io.github.zlatozar.repair.workshop.internal.application.port.in;

import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import java.util.List;

/** What the workshop is working on. */
public interface ViewRepairJobsQuery {

  /** The jobs that are not finished, sorted by ID. */
  List<RepairJobView> openJobs();

  RepairJobView view(RepairJobId job);
}
