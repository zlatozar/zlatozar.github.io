package io.github.zlatozar.repair.department.internal.domain;

import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.AT_WORKSHOP;
import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.USABLE;
import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.WAITING_FOR_REPAIR;
import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.WAITING_FOR_REPLACEMENT;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.Objects;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/**
 * A medical instrument. Its ID, name and owner never change; its state changes only through the
 * {@link Department}, because the transitions are package-private.
 */
@Entity
public final class Instrument {

  @Identity private final InstrumentId id;
  private final String name;
  private final Ownership ownership;
  private InstrumentState state;

  public Instrument(InstrumentId id, String name, Ownership ownership, InstrumentState state) {
    this.id = Objects.requireNonNull(id, "id");
    this.name = Objects.requireNonNull(name, "name");
    this.ownership = Objects.requireNonNull(ownership, "ownership");
    this.state = Objects.requireNonNull(state, "state");
    if (name.isBlank()) {
      throw new IllegalArgumentException("Instrument " + id + " needs a name");
    }
  }

  public InstrumentId id() {
    return id;
  }

  public String name() {
    return name;
  }

  public Ownership ownership() {
    return ownership;
  }

  public InstrumentState state() {
    return state;
  }

  void waitForRepair() {
    move(USABLE, WAITING_FOR_REPAIR);
  }

  void waitForReplacement() {
    move(USABLE, WAITING_FOR_REPLACEMENT);
  }

  void goToWorkshop() {
    move(WAITING_FOR_REPAIR, AT_WORKSHOP);
  }

  void returnToUse() {
    move(AT_WORKSHOP, USABLE);
  }

  private void move(InstrumentState from, InstrumentState to) {
    if (state != from) {
      throw new IllegalStateException("Instrument " + id + " is " + state + ", not " + from);
    }
    state = to;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Instrument instrument && id.equals(instrument.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
