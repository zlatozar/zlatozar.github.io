package io.github.zlatozar.repair.workshop.internal.application.port.in;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.workshop.internal.domain.Damage;
import io.github.zlatozar.repair.workshop.internal.domain.JobState;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJob;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;

/** A read-only snapshot, so callers cannot change a job without a use case. */
public record RepairJobView(
    RepairJobId id, InstrumentId instrument, String department, Damage damage, JobState state) {

  public static RepairJobView of(RepairJob job) {
    return new RepairJobView(
        job.id(), job.instrument(), job.department(), job.damage(), job.state());
  }
}
