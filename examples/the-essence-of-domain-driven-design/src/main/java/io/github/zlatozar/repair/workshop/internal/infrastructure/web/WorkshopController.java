package io.github.zlatozar.repair.workshop.internal.infrastructure.web;

import io.github.zlatozar.repair.workshop.internal.application.port.in.RecordOutcomeCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RecordOutcomeUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.StartRepairCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.StartRepairUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ViewRepairJobsQuery;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.AlreadyFinished;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.NotStarted;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.UnknownRepairJob;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.OpenJobsDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.OutcomeDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.RepairJobDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.WorkshopApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * Parses the input into value objects and calls a use case. The contract already rejected the
 * malformed requests, so a failure here is about meaning: 404, 409 or 422.
 */
@RestController
class WorkshopController implements WorkshopApi {

  private final ViewRepairJobsQuery viewRepairJobs;
  private final StartRepairUseCase startRepair;
  private final RecordOutcomeUseCase recordOutcome;
  private final WorkshopApiMapper mapper;

  WorkshopController(
      ViewRepairJobsQuery viewRepairJobs,
      StartRepairUseCase startRepair,
      RecordOutcomeUseCase recordOutcome,
      WorkshopApiMapper mapper) {
    this.viewRepairJobs = viewRepairJobs;
    this.startRepair = startRepair;
    this.recordOutcome = recordOutcome;
    this.mapper = mapper;
  }

  @Override
  public OpenJobsDto openJobs() {
    return new OpenJobsDto(viewRepairJobs.openJobs().stream().map(mapper::toDto).toList());
  }

  @Override
  public RepairJobDto viewJob(String jobId) {
    return mapper.toDto(viewRepairJobs.view(new RepairJobId(jobId)));
  }

  @Override
  public void startRepair(String jobId) {
    startRepair.startRepair(new StartRepairCommand(new RepairJobId(jobId)));
  }

  @Override
  public void recordOutcome(String jobId, OutcomeDto outcome) {
    recordOutcome.recordOutcome(
        new RecordOutcomeCommand(new RepairJobId(jobId), mapper.outcome(outcome)));
  }

  @ExceptionHandler
  ProblemDetail refused(WorkshopException refusal) {
    var status =
        switch (refusal) {
          case UnknownRepairJob _ -> HttpStatus.NOT_FOUND;
          case NotStarted _, AlreadyFinished _ -> HttpStatus.CONFLICT;
        };
    return ProblemDetail.forStatusAndDetail(status, refusal.getMessage());
  }
}
