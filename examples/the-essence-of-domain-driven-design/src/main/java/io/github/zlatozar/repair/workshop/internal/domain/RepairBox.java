package io.github.zlatozar.repair.workshop.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.List;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * The delivery note of an approved box. In the department a box is an entity with a life cycle;
 * here it is only a list of work, and it is not kept.
 */
@ValueObject
public record RepairBox(String department, int number, List<Item> items) {

  public record Item(InstrumentId instrument, Damage damage) {}

  public RepairBox {
    Objects.requireNonNull(department, "department");
    items = List.copyOf(items);
  }
}
