package io.github.zlatozar.repair.accounting.internal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import io.github.zlatozar.repair.sharedkernel.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Shows how an {@link Asset} adds up its repairs: once per job, and in one currency. */
class AssetTest {

  private static final Repair SHARPENED = new Repair("SURGERY-1-SC-1042", Money.of("45.50", "EUR"));
  private static final Repair SHARPENED_AGAIN =
      new Repair("SURGERY-2-SC-1042", Money.of("20.00", "EUR"));

  private final Asset asset =
      new Asset(
          new InstrumentId("SC-1042"), Money.of("60.00", "EUR"), List.of(), AssetState.ACTIVE);

  @Test
  void theSameRepairIsCountedOnce() {
    asset.recordRepair(SHARPENED);
    asset.recordRepair(SHARPENED);

    assertThat(asset.repairCost()).isEqualTo(Money.of("45.50", "EUR"));
  }

  @Test
  void repairsCanCostMoreThanTheInstrument() {
    asset.recordRepair(SHARPENED);
    assertThat(asset.isUneconomical()).isFalse();

    asset.recordRepair(SHARPENED_AGAIN);

    assertThat(asset.repairCost()).isEqualTo(Money.of("65.50", "EUR"));
    assertThat(asset.isUneconomical()).isTrue();
  }

  @Test
  void aWrittenOffInstrumentStillPaysForItsRepair() {
    asset.writeOff();
    asset.recordRepair(SHARPENED);

    assertThat(asset.state()).isEqualTo(AssetState.WRITTEN_OFF);
    assertThat(asset.repairCost()).isEqualTo(Money.of("45.50", "EUR"));
  }

  @Test
  void aRepairInAnotherCurrencyChangesNothing() {
    var inDollars = new Repair("SURGERY-1-SC-1042", Money.of("45.50", "USD"));

    assertThatThrownBy(() -> asset.recordRepair(inDollars))
        .isInstanceOf(InvalidValueException.class);
    assertThat(asset.repairs()).isEmpty();
    assertThat(asset.repairCost()).isEqualTo(Money.of("0", "EUR"));
  }

  @Test
  void aLoadedAssetKnowsWhatItsRepairsCost() {
    var loaded =
        new Asset(
            asset.id(),
            asset.purchasePrice(),
            List.of(SHARPENED, SHARPENED_AGAIN),
            AssetState.ACTIVE);

    assertThat(loaded.repairCost()).isEqualTo(Money.of("65.50", "EUR"));
  }
}
