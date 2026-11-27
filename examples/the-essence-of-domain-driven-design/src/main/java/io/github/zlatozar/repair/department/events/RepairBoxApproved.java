package io.github.zlatozar.repair.department.events;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.List;
import org.jmolecules.event.annotation.DomainEvent;

/** The director approved a repair box: its instruments go to the workshop. */
@DomainEvent
public record RepairBoxApproved(String department, int box, Category category, List<Item> items) {

  public enum Category {
    SHARPENING,
    MECHANICAL
  }

  public record Item(InstrumentId instrument, Damage damage) {}

  public RepairBoxApproved {
    items = List.copyOf(items);
  }
}
