package io.github.zlatozar.repair.workshop.internal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.Repaired;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.WrittenOff;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent.InstrumentRepaired;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.AlreadyFinished;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.NotStarted;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Shows the life cycle of a {@link RepairJob}: a state machine whose end states hold the result.
 */
class RepairJobTest {

  private static final InstrumentId SCALPEL = new InstrumentId("SC-1042");
  private static final Repaired REPAIRED = new Repaired(Money.of("45.50", "EUR"));

  private final RepairJob job =
      RepairJobFactory.jobsFor(
              new RepairBox(
                  "SURGERY",
                  1,
                  List.of(
                      new RepairBox.Item(SCALPEL, new Damage(Damage.Kind.BLUNT, "Does not cut")))))
          .getFirst();

  @Test
  void aRepairedJobReportsItsCost() {
    job.start();
    job.finish(REPAIRED);

    assertThat(job.state()).isEqualTo(REPAIRED);
    assertThat(job.releaseEvents())
        .containsExactly(
            new InstrumentRepaired(
                new RepairJobId("SURGERY-1-SC-1042"), SCALPEL, Money.of("45.5", "EUR")));
  }

  @Test
  void theSameOutcomeTwiceChangesNothing() {
    job.start();
    job.finish(REPAIRED);
    job.releaseEvents();

    job.finish(REPAIRED);

    assertThat(job.releaseEvents()).isEmpty();
  }

  @Test
  void aFinishedJobKeepsItsOutcome() {
    job.start();
    job.finish(REPAIRED);

    assertThatThrownBy(() -> job.finish(new WrittenOff("Cannot be sharpened again")))
        .isInstanceOf(AlreadyFinished.class);
  }

  @Test
  void aJobFinishesOnlyAfterItStarts() {
    assertThatThrownBy(() -> job.finish(REPAIRED)).isInstanceOf(NotStarted.class);
  }
}
