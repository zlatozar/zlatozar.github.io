package io.github.zlatozar.repair.department.internal.application.port.in;

/** A nurse scans a repaired instrument back into use. Idempotent: a second call changes nothing. */
public interface ReturnFromRepairUseCase {

  void returnFromRepair(ReturnFromRepairCommand command);
}
