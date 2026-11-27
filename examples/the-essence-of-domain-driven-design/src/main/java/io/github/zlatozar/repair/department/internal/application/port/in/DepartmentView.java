package io.github.zlatozar.repair.department.internal.application.port.in;

import io.github.zlatozar.repair.department.internal.domain.Department;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.InstrumentState;
import io.github.zlatozar.repair.department.internal.domain.RepairBox;
import io.github.zlatozar.repair.department.internal.domain.RepairBoxId;
import io.github.zlatozar.repair.department.internal.domain.RepairCategory;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.List;

/**
 * A read-only snapshot, so callers cannot reach the entities that change state. Shows only the open
 * boxes: approved boxes are hidden here, not deleted.
 */
public record DepartmentView(
    DepartmentCode code, List<InstrumentView> instruments, List<RepairBoxView> openBoxes) {

  public record InstrumentView(InstrumentId id, String name, InstrumentState state) {}

  public record RepairBoxView(
      RepairBoxId id, RepairCategory category, List<RepairBox.Item> items) {}

  public static DepartmentView of(Department department) {
    return new DepartmentView(
        department.code(),
        department.instruments().stream()
            .map(instrument ->
                new InstrumentView(instrument.id(), instrument.name(), instrument.state()))
            .toList(),
        department.openBoxes().stream()
            .map(box -> new RepairBoxView(box.id(), box.category(), box.items()))
            .toList());
  }
}
