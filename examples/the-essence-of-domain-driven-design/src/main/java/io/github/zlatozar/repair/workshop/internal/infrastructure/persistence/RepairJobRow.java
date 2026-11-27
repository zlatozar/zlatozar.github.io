package io.github.zlatozar.repair.workshop.internal.infrastructure.persistence;

import static java.util.Objects.requireNonNull;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import io.github.zlatozar.repair.workshop.internal.domain.Damage;
import io.github.zlatozar.repair.workshop.internal.domain.JobState;
import io.github.zlatozar.repair.workshop.internal.domain.JobState.InRepair;
import io.github.zlatozar.repair.workshop.internal.domain.JobState.Received;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.Repaired;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.WrittenOff;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJob;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import java.math.BigDecimal;
import java.util.Currency;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** One row per job. The outcome columns are empty until the job is finished. */
@Table("repair_job")
record RepairJobRow(
    @Id String id,
    String instrument,
    String department,
    Damage.Kind damage,
    String note,
    State state,
    @Nullable BigDecimal costAmount,
    @Nullable String costCurrency,
    @Nullable String reason) {

  enum State {
    RECEIVED,
    IN_REPAIR,
    REPAIRED,
    WRITTEN_OFF
  }

  static RepairJobRow of(RepairJob job) {
    State state =
        switch (job.state()) {
          case Received _ -> State.RECEIVED;
          case InRepair _ -> State.IN_REPAIR;
          case Repaired _ -> State.REPAIRED;
          case WrittenOff _ -> State.WRITTEN_OFF;
        };
    Money cost = job.state() instanceof Repaired(Money paid) ? paid : null;
    String reason = job.state() instanceof WrittenOff(String why) ? why : null;
    return new RepairJobRow(
        job.id().value(),
        job.instrument().value(),
        job.department(),
        job.damage().kind(),
        job.damage().note(),
        state,
        cost == null ? null : cost.amount(),
        cost == null ? null : cost.currency().getCurrencyCode(),
        reason);
  }

  RepairJob toDomain() {
    JobState jobState =
        switch (state) {
          case RECEIVED -> new Received();
          case IN_REPAIR -> new InRepair();
          case REPAIRED ->
              new Repaired(
                  new Money(
                      requireNonNull(costAmount),
                      Currency.getInstance(requireNonNull(costCurrency))));
          case WRITTEN_OFF -> new WrittenOff(requireNonNull(reason));
        };
    return new RepairJob(
        new RepairJobId(id),
        new InstrumentId(instrument),
        department,
        new Damage(damage, note),
        jobState);
  }
}
