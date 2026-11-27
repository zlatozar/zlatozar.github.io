package io.github.zlatozar.repair.workshop.internal.application.service;

import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RecordOutcomeCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RecordOutcomeUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RepairJobView;
import io.github.zlatozar.repair.workshop.internal.application.port.in.StartRepairCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.StartRepairUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ViewRepairJobsQuery;
import io.github.zlatozar.repair.workshop.internal.application.port.out.RepairJobRepository;
import io.github.zlatozar.repair.workshop.internal.application.port.out.WorkshopEventPublisher;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJob;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobFactory;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.UnknownRepairJob;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * One transaction per command. A command changes one job; only a delivery creates several, and new
 * jobs are not shared with anyone yet. Package-private: callers know only the use cases, never this
 * class.
 */
@Service
@Transactional
class WorkshopService
    implements ReceiveRepairBoxUseCase,
        StartRepairUseCase,
        RecordOutcomeUseCase,
        ViewRepairJobsQuery {

  private final RepairJobRepository jobs;
  private final WorkshopEventPublisher events;

  WorkshopService(RepairJobRepository jobs, WorkshopEventPublisher events) {
    this.jobs = jobs;
    this.events = events;
  }

  /**
   * A box delivered again finds its jobs already there and adds nothing. If two deliveries run at
   * the same time, the primary key refuses the second one. Modulith marks that event as failed;
   * nothing is lost, because the first one saved the jobs.
   */
  @Override
  public void receiveRepairBox(ReceiveRepairBoxCommand command) {
    RepairJobFactory.jobsFor(command.box()).stream()
        .filter(job -> !jobs.exists(job.id()))
        .forEach(jobs::add);
  }

  @Override
  public void startRepair(StartRepairCommand command) {
    RepairJob job = load(command.job());
    job.start();
    save(job);
  }

  @Override
  public void recordOutcome(RecordOutcomeCommand command) {
    RepairJob job = load(command.job());
    job.finish(command.outcome());
    save(job);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RepairJobView> openJobs() {
    return jobs.findOpen().stream().map(RepairJobView::of).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public RepairJobView view(RepairJobId id) {
    return jobs.findById(id).map(RepairJobView::of).orElseThrow(() -> new UnknownRepairJob(id));
  }

  private RepairJob load(RepairJobId id) {
    return jobs.findByIdForUpdate(id).orElseThrow(() -> new UnknownRepairJob(id));
  }

  /** Saves first: when saving fails, no event leaves the workshop. */
  private void save(RepairJob job) {
    jobs.save(job);
    job.releaseEvents().forEach(events::publish);
  }
}
