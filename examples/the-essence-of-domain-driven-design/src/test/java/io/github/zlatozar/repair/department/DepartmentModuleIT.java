package io.github.zlatozar.repair.department;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zlatozar.repair.department.events.RepairBoxApproved;
import io.github.zlatozar.repair.department.events.ReplacementRequested;
import io.github.zlatozar.repair.department.internal.application.port.in.ApproveRepairBoxCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.ApproveRepairBoxUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.DepartmentView;
import io.github.zlatozar.repair.department.internal.application.port.in.SendForRepairCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.SendForRepairUseCase;
import io.github.zlatozar.repair.department.internal.application.port.in.ViewDepartmentQuery;
import io.github.zlatozar.repair.department.internal.domain.Damage;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.InstrumentState;
import io.github.zlatozar.repair.department.internal.domain.RepairBoxId;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.transaction.annotation.Transactional;

/**
 * Starts only the department module, with the seed data from {@code data.sql}. Each test rolls
 * back, so the next one finds the department unchanged.
 */
@ApplicationModuleTest
@Transactional
class DepartmentModuleIT {

  private static final DepartmentCode SURGERY = new DepartmentCode("SURGERY");
  private static final InstrumentId SCALPEL = new InstrumentId("SC-1042");
  private static final InstrumentId ENDOSCOPE = new InstrumentId("EN-7");

  @Autowired SendForRepairUseCase sendForRepair;
  @Autowired ApproveRepairBoxUseCase approveRepairBox;
  @Autowired ViewDepartmentQuery viewDepartment;

  @Test
  void anApprovedBoxGoesToTheWorkshop(AssertablePublishedEvents events) {
    sendForRepair.sendForRepair(
        new SendForRepairCommand(SURGERY, SCALPEL, new Damage(Damage.Kind.BLUNT, "Does not cut")));
    approveRepairBox.approveRepairBox(new ApproveRepairBoxCommand(SURGERY, new RepairBoxId(1)));

    assertThat(events)
        .contains(RepairBoxApproved.class)
        .matchingValue(RepairBoxApproved::department, "SURGERY")
        .matchingValue(RepairBoxApproved::category, RepairBoxApproved.Category.SHARPENING);

    DepartmentView surgery = viewDepartment.view(SURGERY);
    assertThat(surgery.openBoxes()).isEmpty();
    assertThat(surgery.instruments())
        .filteredOn(instrument -> instrument.id().equals(SCALPEL))
        .singleElement()
        .satisfies(scalpel -> assertThat(scalpel.state()).isEqualTo(InstrumentState.AT_WORKSHOP));
  }

  @Test
  void aBrokenThirdPartyInstrumentIsReplaced(AssertablePublishedEvents events) {
    sendForRepair.sendForRepair(
        new SendForRepairCommand(
            SURGERY, ENDOSCOPE, new Damage(Damage.Kind.BROKEN, "The lens is cracked")));

    assertThat(events)
        .contains(ReplacementRequested.class)
        .matchingValue(ReplacementRequested::supplier, "MedLease");
    assertThat(viewDepartment.view(SURGERY).openBoxes()).isEmpty();
  }
}
