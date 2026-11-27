package io.github.zlatozar.repair.workshop.internal.application.port.in;

/** A technician starts a repair. Idempotent: a second call changes nothing. */
public interface StartRepairUseCase {

  void startRepair(StartRepairCommand command);
}
