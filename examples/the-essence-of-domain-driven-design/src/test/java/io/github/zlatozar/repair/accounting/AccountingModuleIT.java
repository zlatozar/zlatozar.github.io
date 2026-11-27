package io.github.zlatozar.repair.accounting;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zlatozar.repair.accounting.internal.application.port.in.AssetView;
import io.github.zlatozar.repair.accounting.internal.application.port.in.RecordRepairCommand;
import io.github.zlatozar.repair.accounting.internal.application.port.in.RecordRepairUseCase;
import io.github.zlatozar.repair.accounting.internal.application.port.in.ViewAssetQuery;
import io.github.zlatozar.repair.accounting.internal.domain.AssetState;
import io.github.zlatozar.repair.accounting.internal.domain.Repair;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import io.github.zlatozar.repair.workshop.events.InstrumentRepaired;
import io.github.zlatozar.repair.workshop.events.InstrumentWrittenOff;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.core.EventSerializer;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

/**
 * Starts only the accounting module; the workshop events are published by hand. No rollback here: a
 * module listener runs only after a commit. The data stays, so the tests do not share an asset.
 */
@ApplicationModuleTest
class AccountingModuleIT {

  private static final InstrumentId SCALPEL = new InstrumentId("SC-1042");
  private static final InstrumentId FORCEPS = new InstrumentId("FO-2001");

  @Autowired RecordRepairUseCase recordRepair;
  @Autowired ViewAssetQuery viewAsset;

  @Test
  void aRepairAddsToTheCostOfTheInstrument(Scenario scenario) {
    scenario
        .publish(new InstrumentRepaired("SURGERY-1-SC-1042", SCALPEL, Money.of("45.50", "EUR")))
        .andWaitForStateChange(() -> viewAsset.view(SCALPEL), AccountingModuleIT::hasRepairs)
        .andVerify(asset -> assertThat(asset.repairCost()).isEqualTo(Money.of("45.50", "EUR")));
  }

  @Test
  void aRepairReportedTwiceIsPaidOnce() {
    var repair =
        new RecordRepairCommand(FORCEPS, new Repair("SURGERY-2-FO-2001", Money.of("20", "EUR")));

    recordRepair.recordRepair(repair);
    recordRepair.recordRepair(repair);

    assertThat(viewAsset.view(FORCEPS).repairCost()).isEqualTo(Money.of("20", "EUR"));
  }

  @Test
  void anInstrumentTheWorkshopGivesUpIsWrittenOff(Scenario scenario) {
    scenario
        .publish(new InstrumentWrittenOff("SURGERY-3-FO-2001", FORCEPS, "Cracked jaw"))
        .andWaitForStateChange(
            () -> viewAsset.view(FORCEPS).state(), AssetState.WRITTEN_OFF::equals)
        .andVerify(state -> assertThat(state).isEqualTo(AssetState.WRITTEN_OFF));
  }

  /** A failed event waits in the event table as JSON. It must come back as the same event. */
  @Test
  void aStoredEventCanBeReadBack(@Autowired EventSerializer serializer) {
    var event = new InstrumentRepaired("SURGERY-4-SC-1042", SCALPEL, Money.of("45.50", "EUR"));

    assertThat(serializer.deserialize(serializer.serialize(event), InstrumentRepaired.class))
        .isEqualTo(event);
  }

  private static boolean hasRepairs(AssetView asset) {
    return asset.repairCost().amount().signum() > 0;
  }
}
