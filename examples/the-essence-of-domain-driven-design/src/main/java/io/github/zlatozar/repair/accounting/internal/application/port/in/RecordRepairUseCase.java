package io.github.zlatozar.repair.accounting.internal.application.port.in;

/** Adds the cost of a finished repair to an asset. Idempotent: a second call changes nothing. */
public interface RecordRepairUseCase {

  void recordRepair(RecordRepairCommand command);
}
