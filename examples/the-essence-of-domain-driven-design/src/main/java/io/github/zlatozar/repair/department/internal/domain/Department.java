package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent.RepairBoxApproved;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent.ReplacementRequested;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.NotAtWorkshop;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.UnknownInstrument;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.UnknownRepairBox;
import io.github.zlatozar.repair.department.internal.domain.Triage.Repair;
import io.github.zlatozar.repair.department.internal.domain.Triage.Replace;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * A hospital department with its instruments and repair boxes. The gateway: every change to them
 * goes through one of its commands, and every command is idempotent.
 */
@AggregateRoot
public final class Department {

  @Identity private final DepartmentCode code;
  private final Map<InstrumentId, Instrument> instruments = new LinkedHashMap<>();
  private final Map<RepairBoxId, RepairBox> boxes = new LinkedHashMap<>();
  private final List<DepartmentEvent> events = new ArrayList<>();

  public Department(
      DepartmentCode code, Collection<Instrument> instruments, Collection<RepairBox> boxes) {
    this.code = Objects.requireNonNull(code, "code");
    instruments.forEach(instrument -> this.instruments.put(instrument.id(), instrument));
    boxes.forEach(box -> this.boxes.put(box.id(), box));
  }

  /** Marks the instrument unusable, then puts it in a repair box or asks for a new one. */
  public void sendForRepair(InstrumentId id, Damage damage) {
    Instrument instrument = instrument(id);
    if (instrument.state() != InstrumentState.USABLE) {
      return;
    }
    switch (Triage.classify(instrument.ownership(), damage)) {
      case Repair(RepairCategory category) -> {
        instrument.waitForRepair();
        putInBox(new RepairBox.Item(id, damage), category);
      }
      case Replace(Supplier supplier) -> {
        instrument.waitForReplacement();
        events.add(new ReplacementRequested(code, id, supplier, damage));
      }
    }
  }

  /** The director's approval: the box and its instruments go to the workshop. */
  public void approveRepairBox(RepairBoxId id) {
    RepairBox box = box(id);
    if (box.state() == RepairBox.State.APPROVED) {
      return;
    }
    box.approve();
    box.items().forEach(item -> instrument(item.instrument()).goToWorkshop());
    events.add(new RepairBoxApproved(code, id, box.category(), box.items()));
  }

  /** A repaired instrument is back and usable again. */
  public void returnFromRepair(InstrumentId id) {
    Instrument instrument = instrument(id);
    switch (instrument.state()) {
      case AT_WORKSHOP -> instrument.returnToUse();
      case USABLE -> {
        // Already returned
      }
      case WAITING_FOR_REPAIR, WAITING_FOR_REPLACEMENT ->
          throw new NotAtWorkshop(id, instrument.state());
    }
  }

  /** Hands over the events since the last call; the caller publishes them after saving. */
  public List<DepartmentEvent> releaseEvents() {
    List<DepartmentEvent> released = List.copyOf(events);
    events.clear();
    return released;
  }

  public DepartmentCode code() {
    return code;
  }

  public List<Instrument> instruments() {
    return List.copyOf(instruments.values());
  }

  public Instrument instrument(InstrumentId id) {
    Instrument instrument = instruments.get(id);
    if (instrument == null) {
      throw new UnknownInstrument(code, id);
    }
    return instrument;
  }

  /** All boxes, approved ones too: they are history, and history is kept. */
  public List<RepairBox> boxes() {
    return List.copyOf(boxes.values());
  }

  /** The boxes waiting for the director. Views only hide things, never remove them. */
  public List<RepairBox> openBoxes() {
    return boxes.values().stream().filter(box -> box.state() == RepairBox.State.OPEN).toList();
  }

  private RepairBox box(RepairBoxId id) {
    RepairBox box = boxes.get(id);
    if (box == null) {
      throw new UnknownRepairBox(code, id);
    }
    return box;
  }

  private void putInBox(RepairBox.Item item, RepairCategory category) {
    boxes.values().stream()
        .filter(box -> box.accepts(category))
        .findFirst()
        .ifPresentOrElse(box -> box.put(item), () -> openBox(item, category));
  }

  private void openBox(RepairBox.Item item, RepairCategory category) {
    int last = boxes.keySet().stream().mapToInt(RepairBoxId::number).max().orElse(0);
    RepairBoxId id = new RepairBoxId(last + 1);
    boxes.put(id, RepairBox.open(id, category, item));
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Department department && code.equals(department.code);
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }
}
