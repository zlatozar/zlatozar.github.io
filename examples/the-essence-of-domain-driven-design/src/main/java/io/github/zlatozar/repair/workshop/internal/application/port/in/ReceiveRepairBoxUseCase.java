package io.github.zlatozar.repair.workshop.internal.application.port.in;

/**
 * An approved box arrives: each instrument in it becomes a repair job. Idempotent: a second call
 * changes nothing.
 */
public interface ReceiveRepairBoxUseCase {

  void receiveRepairBox(ReceiveRepairBoxCommand command);
}
