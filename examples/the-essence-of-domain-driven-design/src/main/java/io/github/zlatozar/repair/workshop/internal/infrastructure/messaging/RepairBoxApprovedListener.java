package io.github.zlatozar.repair.workshop.internal.infrastructure.messaging;

import io.github.zlatozar.repair.department.events.RepairBoxApproved;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * The driving adapter for events. Runs after the department's transaction commits, in a new one of
 * its own: a failure here cannot undo the approval, and Modulith keeps the event, marked as failed.
 */
@Component
class RepairBoxApprovedListener {

  private final ReceiveRepairBoxUseCase receiveRepairBox;
  private final RepairBoxApprovedMapper mapper;

  RepairBoxApprovedListener(
      ReceiveRepairBoxUseCase receiveRepairBox, RepairBoxApprovedMapper mapper) {
    this.receiveRepairBox = receiveRepairBox;
    this.mapper = mapper;
  }

  @ApplicationModuleListener
  void on(RepairBoxApproved event) {
    receiveRepairBox.receiveRepairBox(new ReceiveRepairBoxCommand(mapper.toRepairBox(event)));
  }
}
