package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jmolecules.ddd.annotation.ValueObject;

/** "A box contains one or more medical instruments", all of one repair category. */
@Entity
public final class RepairBox {

  /** Small on purpose, so the demo soon fills a box and opens the next one. */
  public static final int CAPACITY = 3;

  public enum State {
    OPEN,
    APPROVED
  }

  @ValueObject
  public record Item(InstrumentId instrument, Damage damage) {}

  @Identity private final RepairBoxId id;
  private final RepairCategory category;
  private final List<Item> items;
  private State state;

  public RepairBox(RepairBoxId id, RepairCategory category, List<Item> items, State state) {
    this.id = Objects.requireNonNull(id, "id");
    this.category = Objects.requireNonNull(category, "category");
    this.items = new ArrayList<>(items);
    this.state = Objects.requireNonNull(state, "state");
    if (items.isEmpty() || items.size() > CAPACITY) {
      throw new IllegalArgumentException(
          "Box " + id + " must hold 1 to " + CAPACITY + " instruments, not " + items.size());
    }
  }

  static RepairBox open(RepairBoxId id, RepairCategory category, Item first) {
    return new RepairBox(id, category, List.of(first), State.OPEN);
  }

  public RepairBoxId id() {
    return id;
  }

  public RepairCategory category() {
    return category;
  }

  public List<Item> items() {
    return List.copyOf(items);
  }

  public State state() {
    return state;
  }

  boolean accepts(RepairCategory category) {
    return state == State.OPEN && this.category == category && items.size() < CAPACITY;
  }

  void put(Item item) {
    if (!accepts(this.category)) {
      throw new IllegalStateException("Box " + id + " is full or approved");
    }
    items.add(item);
  }

  void approve() {
    state = State.APPROVED;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof RepairBox box && id.equals(box.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
