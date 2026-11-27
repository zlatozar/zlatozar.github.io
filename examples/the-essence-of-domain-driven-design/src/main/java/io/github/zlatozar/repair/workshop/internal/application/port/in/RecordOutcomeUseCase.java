package io.github.zlatozar.repair.workshop.internal.application.port.in;

/**
 * A technician finishes a repair: repaired at a cost, or written off. Idempotent: a second call
 * changes nothing.
 */
public interface RecordOutcomeUseCase {

  void recordOutcome(RecordOutcomeCommand command);
}
