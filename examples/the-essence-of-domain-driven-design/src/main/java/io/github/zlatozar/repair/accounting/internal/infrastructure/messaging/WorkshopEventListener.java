package io.github.zlatozar.repair.accounting.internal.infrastructure.messaging;

import io.github.zlatozar.repair.accounting.internal.application.port.in.RecordRepairCommand;
import io.github.zlatozar.repair.accounting.internal.application.port.in.RecordRepairUseCase;
import io.github.zlatozar.repair.accounting.internal.application.port.in.WriteOffAssetUseCase;
import io.github.zlatozar.repair.accounting.internal.application.port.in.WriteOffCommand;
import io.github.zlatozar.repair.accounting.internal.domain.Repair;
import io.github.zlatozar.repair.workshop.events.InstrumentRepaired;
import io.github.zlatozar.repair.workshop.events.InstrumentWrittenOff;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * The driving adapter for workshop events. If a command fails, Modulith keeps the event in the
 * event table, marked as failed, and the workshop is not affected. Resending, not implemented in
 * this project, helps only when the cause goes away, like a busy database. An unknown asset stays
 * unknown, so a person must check it.
 */
@Component
class WorkshopEventListener {

  private final RecordRepairUseCase recordRepair;
  private final WriteOffAssetUseCase writeOff;

  WorkshopEventListener(RecordRepairUseCase recordRepair, WriteOffAssetUseCase writeOff) {
    this.recordRepair = recordRepair;
    this.writeOff = writeOff;
  }

  @ApplicationModuleListener
  void on(InstrumentRepaired event) {
    recordRepair.recordRepair(
        new RecordRepairCommand(event.instrument(), new Repair(event.job(), event.cost())));
  }

  /** The reason is for the workshop's records; accounting needs only the fact. */
  @ApplicationModuleListener
  void on(InstrumentWrittenOff event) {
    writeOff.writeOff(new WriteOffCommand(event.instrument()));
  }
}
