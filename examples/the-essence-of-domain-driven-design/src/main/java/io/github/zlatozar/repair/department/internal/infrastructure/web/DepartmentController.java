package io.github.zlatozar.repair.department.internal.infrastructure.web;

import io.github.zlatozar.repair.department.internal.application.port.in.ApproveRepairBoxCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.ApproveRepairBoxUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.ReturnFromRepairCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.ReturnFromRepairUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.SendForRepairCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.SendForRepairUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.ViewDepartmentQuery;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.NotAtWorkshop;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.UnknownDepartment;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.UnknownInstrument;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.UnknownRepairBox;
import io.github.zlatozar.repair.department.internal.domain.RepairBoxId;
import io.github.zlatozar.repair.department.internal.infrastructure.web.generated.DamageDto;
import io.github.zlatozar.repair.department.internal.infrastructure.web.generated.DepartmentApi;
import io.github.zlatozar.repair.department.internal.infrastructure.web.generated.DepartmentDto;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * Parses the input into value objects and calls a use case. The contract already rejected the
 * malformed requests, so a failure here is about meaning: 404, 409 or 422.
 */
@RestController
class DepartmentController implements DepartmentApi {

  private final ViewDepartmentQuery viewDepartment;
  private final SendForRepairUseCase sendForRepair;
  private final ApproveRepairBoxUseCase approveRepairBox;
  private final ReturnFromRepairUseCase returnFromRepair;
  private final DepartmentApiMapper mapper;

  DepartmentController(
      ViewDepartmentQuery viewDepartment,
      SendForRepairUseCase sendForRepair,
      ApproveRepairBoxUseCase approveRepairBox,
      ReturnFromRepairUseCase returnFromRepair,
      DepartmentApiMapper mapper) {
    this.viewDepartment = viewDepartment;
    this.sendForRepair = sendForRepair;
    this.approveRepairBox = approveRepairBox;
    this.returnFromRepair = returnFromRepair;
    this.mapper = mapper;
  }

  @Override
  public DepartmentDto viewDepartment(String code) {
    return mapper.toDto(viewDepartment.view(new DepartmentCode(code)));
  }

  @Override
  public void sendForRepair(String code, String instrumentId, DamageDto damage) {
    sendForRepair.sendForRepair(
        new SendForRepairCommand(
            new DepartmentCode(code), new InstrumentId(instrumentId), mapper.toDamage(damage)));
  }

  @Override
  public void approveRepairBox(String code, Integer boxId) {
    approveRepairBox.approveRepairBox(
        new ApproveRepairBoxCommand(new DepartmentCode(code), new RepairBoxId(boxId)));
  }

  @Override
  public void returnFromRepair(String code, String instrumentId) {
    returnFromRepair.returnFromRepair(
        new ReturnFromRepairCommand(new DepartmentCode(code), new InstrumentId(instrumentId)));
  }

  @ExceptionHandler
  ProblemDetail refused(DepartmentException refusal) {
    var status =
        switch (refusal) {
          case UnknownDepartment _, UnknownInstrument _, UnknownRepairBox _ -> HttpStatus.NOT_FOUND;
          case NotAtWorkshop _ -> HttpStatus.CONFLICT;
        };
    return ProblemDetail.forStatusAndDetail(status, refusal.getMessage());
  }
}
