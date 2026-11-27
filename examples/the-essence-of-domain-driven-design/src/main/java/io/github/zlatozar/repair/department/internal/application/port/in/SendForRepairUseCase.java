package io.github.zlatozar.repair.department.internal.application.port.in;

/** A nurse sends a damaged instrument for repair. Idempotent: a second call changes nothing. */
public interface SendForRepairUseCase {

  void sendForRepair(SendForRepairCommand command);
}
