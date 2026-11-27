package io.github.zlatozar.repair.department.internal.application.port.in;

/**
 * The director approves a repair box, and its instruments go to the workshop. Idempotent: a second
 * call changes nothing.
 */
public interface ApproveRepairBoxUseCase {

  void approveRepairBox(ApproveRepairBoxCommand command);
}
