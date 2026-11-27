package io.github.zlatozar.repair.department.internal.domain;

import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.AT_WORKSHOP;
import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.USABLE;
import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.WAITING_FOR_REPAIR;
import static io.github.zlatozar.repair.department.internal.domain.InstrumentState.WAITING_FOR_REPLACEMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.zlatozar.repair.department.internal.domain.Damage.Kind;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent.RepairBoxApproved;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent.ReplacementRequested;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.NotAtWorkshop;
import io.github.zlatozar.repair.department.internal.domain.Ownership.Ours;
import io.github.zlatozar.repair.department.internal.domain.Ownership.ThirdParty;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Shows the aggregate rules from the post on {@link Department}. */
class DepartmentTest {

  private static final DepartmentCode SURGERY = new DepartmentCode("SURGERY");
  private static final InstrumentId SCALPEL = new InstrumentId("SC-1042");
  private static final InstrumentId FORCEPS = new InstrumentId("FO-2001");
  private static final InstrumentId ENDOSCOPE = new InstrumentId("EN-7");
  private static final Supplier MED_LEASE = new Supplier("MedLease");

  private static final Damage BLUNT = new Damage(Kind.BLUNT, "Does not cut");
  private static final Damage BENT = new Damage(Kind.BENT, "Tips do not meet");

  private final Department surgery =
      new Department(
          SURGERY,
          List.of(
              ours(SCALPEL, "Scalpel"),
              ours(FORCEPS, "Forceps"),
              new Instrument(ENDOSCOPE, "Endoscope", new ThirdParty(MED_LEASE), USABLE)),
          List.of());

  @Test
  void sortsInstrumentsIntoBoxesByRepairCategory() {
    surgery.sendForRepair(SCALPEL, BLUNT);
    surgery.sendForRepair(FORCEPS, BENT);

    assertThat(surgery.openBoxes())
        .extracting(RepairBox::category)
        .containsExactly(RepairCategory.SHARPENING, RepairCategory.MECHANICAL);
    assertThat(surgery.instrument(SCALPEL).state()).isEqualTo(WAITING_FOR_REPAIR);
  }

  @Test
  void opensANewBoxWhenTheOpenOneIsFull() {
    Department department = new Department(SURGERY, scalpels(RepairBox.CAPACITY + 1), List.of());

    department.instruments().forEach(scalpel -> department.sendForRepair(scalpel.id(), BLUNT));

    assertThat(department.openBoxes())
        .extracting(box -> box.items().size())
        .containsExactly(RepairBox.CAPACITY, 1);
  }

  @Test
  void asksTheOwnerToReplaceAThirdPartyInstrument() {
    surgery.sendForRepair(ENDOSCOPE, BENT);

    assertThat(surgery.instrument(ENDOSCOPE).state()).isEqualTo(WAITING_FOR_REPLACEMENT);
    assertThat(surgery.openBoxes()).isEmpty();
    assertThat(surgery.releaseEvents())
        .containsExactly(new ReplacementRequested(SURGERY, ENDOSCOPE, MED_LEASE, BENT));
  }

  @Test
  void sendingTwiceChangesNothing() {
    surgery.sendForRepair(SCALPEL, BLUNT);
    surgery.sendForRepair(SCALPEL, BLUNT);

    assertThat(surgery.openBoxes())
        .singleElement()
        .extracting(box -> box.items().size())
        .isEqualTo(1);
  }

  @Test
  void approvedBoxGoesToTheWorkshopAndLeavesTheOpenView() {
    surgery.sendForRepair(SCALPEL, BLUNT);
    RepairBoxId box = surgery.openBoxes().getFirst().id();

    surgery.approveRepairBox(box);

    assertThat(surgery.instrument(SCALPEL).state()).isEqualTo(AT_WORKSHOP);
    assertThat(surgery.openBoxes()).isEmpty();
    assertThat(surgery.boxes()).hasSize(1);
    assertThat(surgery.releaseEvents())
        .containsExactly(
            new RepairBoxApproved(
                SURGERY,
                box,
                RepairCategory.SHARPENING,
                List.of(new RepairBox.Item(SCALPEL, BLUNT))));
  }

  @Test
  void onlyAnInstrumentAtTheWorkshopCanReturn() {
    surgery.sendForRepair(SCALPEL, BLUNT);

    assertThatThrownBy(() -> surgery.returnFromRepair(SCALPEL)).isInstanceOf(NotAtWorkshop.class);

    surgery.approveRepairBox(surgery.openBoxes().getFirst().id());
    surgery.returnFromRepair(SCALPEL);

    assertThat(surgery.instrument(SCALPEL).state()).isEqualTo(USABLE);
  }

  private static Instrument ours(InstrumentId id, String name) {
    return new Instrument(id, name, new Ours(), USABLE);
  }

  private static List<Instrument> scalpels(int count) {
    return IntStream.rangeClosed(1, count)
        .mapToObj(number -> ours(new InstrumentId("SC-" + number), "Scalpel " + number))
        .toList();
  }
}
