package io.github.zlatozar.repair.workshop.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import io.github.zlatozar.repair.workshop.internal.domain.JobState.InRepair;
import io.github.zlatozar.repair.workshop.internal.domain.JobState.Received;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.Repaired;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.WrittenOff;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent.InstrumentRepaired;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent.InstrumentWrittenOff;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.AlreadyFinished;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopException.NotStarted;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * One instrument in the workshop, from its arrival to the outcome. A small aggregate: technicians
 * work on many instruments at the same time, and a change locks only its own job.
 */
@AggregateRoot
public final class RepairJob {

  @Identity private final RepairJobId id;
  private final InstrumentId instrument;
  private final String department; // Only a return address: the workshop has no rules about it
  private final Damage damage;
  private JobState state;
  private final List<WorkshopEvent> events = new ArrayList<>();

  public RepairJob(
      RepairJobId id, InstrumentId instrument, String department, Damage damage, JobState state) {
    this.id = Objects.requireNonNull(id, "id");
    this.instrument = Objects.requireNonNull(instrument, "instrument");
    this.department = Objects.requireNonNull(department, "department");
    this.damage = Objects.requireNonNull(damage, "damage");
    this.state = Objects.requireNonNull(state, "state");
  }

  /** The technician takes the instrument out of its box. */
  public void start() {
    switch (state) {
      case Received _ -> state = new InRepair();
      case InRepair _ -> {
        // Already started
      }
      case Outcome _ -> throw new AlreadyFinished(id);
    }
  }

  /** Ends the repair. The same outcome again changes nothing; a different one is refused. */
  public void finish(Outcome outcome) {
    Objects.requireNonNull(outcome, "outcome");
    switch (state) {
      case Received _ -> throw new NotStarted(id);
      case InRepair _ -> {
        state = outcome;
        events.add(
            switch (outcome) {
              case Repaired(Money cost) -> new InstrumentRepaired(id, instrument, cost);
              case WrittenOff(String reason) -> new InstrumentWrittenOff(id, instrument, reason);
            });
      }
      case Outcome finished when finished.equals(outcome) -> {
        // Already finished this way
      }
      case Outcome _ -> throw new AlreadyFinished(id);
    }
  }

  /** Hands over the events since the last call; the caller publishes them after saving. */
  public List<WorkshopEvent> releaseEvents() {
    List<WorkshopEvent> released = List.copyOf(events);
    events.clear();
    return released;
  }

  public RepairJobId id() {
    return id;
  }

  public InstrumentId instrument() {
    return instrument;
  }

  public String department() {
    return department;
  }

  public Damage damage() {
    return damage;
  }

  public JobState state() {
    return state;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof RepairJob job && id.equals(job.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
