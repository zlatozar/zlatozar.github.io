package io.github.zlatozar.repair.department.internal.infrastructure.persistence;

import static java.util.stream.Collectors.toMap;

import io.github.zlatozar.repair.department.internal.domain.Damage;
import io.github.zlatozar.repair.department.internal.domain.Department;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.Instrument;
import io.github.zlatozar.repair.department.internal.domain.InstrumentState;
import io.github.zlatozar.repair.department.internal.domain.Ownership;
import io.github.zlatozar.repair.department.internal.domain.Ownership.Ours;
import io.github.zlatozar.repair.department.internal.domain.Ownership.ThirdParty;
import io.github.zlatozar.repair.department.internal.domain.RepairBox;
import io.github.zlatozar.repair.department.internal.domain.RepairBoxId;
import io.github.zlatozar.repair.department.internal.domain.RepairCategory;
import io.github.zlatozar.repair.department.internal.domain.Supplier;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

/**
 * The whole aggregate in one object: Spring Data JDBC loads and saves it with its children. The map
 * keys are the natural keys, so the tables are easy to read and to fix by hand.
 */
@Table("department")
record DepartmentRow(
    @Id String code,
    @MappedCollection(idColumn = "department", keyColumn = "id")
        Map<String, InstrumentRow> instruments,
    @MappedCollection(idColumn = "department", keyColumn = "box_number")
        Map<Integer, RepairBoxRow> boxes) {

  static DepartmentRow of(Department department) {
    return new DepartmentRow(
        department.code().value(),
        department.instruments().stream()
            .collect(toMap(instrument -> instrument.id().value(), InstrumentRow::of)),
        department.boxes().stream().collect(toMap(box -> box.id().number(), RepairBoxRow::of)));
  }

  /** Sorted by key: the oldest open box must come first, as it did before saving. */
  Department toDomain() {
    return new Department(
        new DepartmentCode(code),
        instruments.entrySet().stream()
            .sorted(Entry.comparingByKey())
            .map(entry -> entry.getValue().toDomain(new InstrumentId(entry.getKey())))
            .toList(),
        boxes.entrySet().stream()
            .sorted(Entry.comparingByKey())
            .map(entry -> entry.getValue().toDomain(new RepairBoxId(entry.getKey())))
            .toList());
  }

  /** No supplier means the hospital owns the instrument. */
  @Table("instrument")
  record InstrumentRow(String name, @Nullable String supplier, InstrumentState state) {

    static InstrumentRow of(Instrument instrument) {
      String supplier =
          switch (instrument.ownership()) {
            case Ours() -> null;
            case ThirdParty(Supplier owner) -> owner.name();
          };
      return new InstrumentRow(instrument.name(), supplier, instrument.state());
    }

    Instrument toDomain(InstrumentId id) {
      Ownership ownership = supplier == null ? new Ours() : new ThirdParty(new Supplier(supplier));
      return new Instrument(id, name, ownership, state);
    }
  }

  @Table("repair_box")
  record RepairBoxRow(
      RepairCategory category,
      RepairBox.State state,
      @MappedCollection(idColumn = "department", keyColumn = "position") List<ItemRow> items) {

    static RepairBoxRow of(RepairBox box) {
      return new RepairBoxRow(
          box.category(), box.state(), box.items().stream().map(ItemRow::of).toList());
    }

    RepairBox toDomain(RepairBoxId id) {
      return new RepairBox(id, category, items.stream().map(ItemRow::toDomain).toList(), state);
    }
  }

  @Table("repair_box_item")
  record ItemRow(String instrument, Damage.Kind damage, String note) {

    static ItemRow of(RepairBox.Item item) {
      return new ItemRow(item.instrument().value(), item.damage().kind(), item.damage().note());
    }

    RepairBox.Item toDomain() {
      return new RepairBox.Item(new InstrumentId(instrument), new Damage(damage, note));
    }
  }
}
