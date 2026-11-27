package io.github.zlatozar.repair.workshop;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zlatozar.repair.department.events.RepairBoxApproved;
import io.github.zlatozar.repair.department.events.RepairBoxApproved.Category;
import io.github.zlatozar.repair.department.events.RepairBoxApproved.Item;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import io.github.zlatozar.repair.workshop.events.InstrumentRepaired;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RecordOutcomeCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RecordOutcomeUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RepairJobView;
import io.github.zlatozar.repair.workshop.internal.application.port.in.StartRepairCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.StartRepairUseCase;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ViewRepairJobsQuery;
import io.github.zlatozar.repair.workshop.internal.domain.Damage;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.Repaired;
import io.github.zlatozar.repair.workshop.internal.domain.RepairBox;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.modulith.test.Scenario;

/**
 * Starts only the workshop module. No rollback here: a module listener runs only after a commit.
 * The data stays, so every test uses its own box.
 */
@ApplicationModuleTest
class WorkshopModuleIT {

  private static final InstrumentId SCALPEL = new InstrumentId("SC-1042");
  private static final InstrumentId FORCEPS = new InstrumentId("FO-2001");
  private static final Damage BLUNT = new Damage(Damage.Kind.BLUNT, "Does not cut");

  @Autowired ReceiveRepairBoxUseCase receiveRepairBox;
  @Autowired StartRepairUseCase startRepair;
  @Autowired RecordOutcomeUseCase recordOutcome;
  @Autowired ViewRepairJobsQuery viewRepairJobs;

  @Test
  void anApprovedBoxBecomesRepairJobs(Scenario scenario) {
    // Each context has its own Damage: this one is the department's, from its published event
    var reported =
        new io.github.zlatozar.repair.department.events.Damage(
            io.github.zlatozar.repair.department.events.Damage.Kind.BLUNT, "Does not cut");

    scenario
        .publish(
            new RepairBoxApproved(
                "SURGERY",
                41,
                Category.SHARPENING,
                List.of(new Item(SCALPEL, reported), new Item(FORCEPS, reported))))
        .andWaitForStateChange(() -> jobsOfBox(41), jobs -> jobs.size() == 2)
        .andVerify(jobs ->
            assertThat(jobs)
                .extracting(job -> job.id().value())
                .containsExactly("SURGERY-41-FO-2001", "SURGERY-41-SC-1042"));
  }

  @Test
  void aBoxDeliveredTwiceGivesTheSameJobs() {
    receiveRepairBox.receiveRepairBox(box(42));
    receiveRepairBox.receiveRepairBox(box(42));

    assertThat(jobsOfBox(42)).hasSize(1);
  }

  @Test
  void aRepairedInstrumentIsAnnounced(AssertablePublishedEvents events) {
    receiveRepairBox.receiveRepairBox(box(43));
    var job = new RepairJobId("SURGERY-43-SC-1042");

    startRepair.startRepair(new StartRepairCommand(job));
    recordOutcome.recordOutcome(
        new RecordOutcomeCommand(job, new Repaired(Money.of("45.50", "EUR"))));

    assertThat(events)
        .contains(InstrumentRepaired.class)
        .matchingValue(InstrumentRepaired::job, job.value())
        .matchingValue(InstrumentRepaired::cost, Money.of("45.50", "EUR"));
  }

  private List<RepairJobView> jobsOfBox(int box) {
    return viewRepairJobs.openJobs().stream()
        .filter(job -> job.id().value().startsWith("SURGERY-" + box + "-"))
        .toList();
  }

  private static ReceiveRepairBoxCommand box(int number) {
    return new ReceiveRepairBoxCommand(
        new RepairBox("SURGERY", number, List.of(new RepairBox.Item(SCALPEL, BLUNT))));
  }
}
