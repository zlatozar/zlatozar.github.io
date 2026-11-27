package io.github.zlatozar.repair.department.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.zlatozar.repair.department.internal.application.port.in.ApproveRepairBoxCommand;
import io.github.zlatozar.repair.department.internal.application.port.in.SendForRepairCommand;
import io.github.zlatozar.repair.department.internal.application.port.out.DepartmentRepository;
import io.github.zlatozar.repair.department.internal.domain.Damage;
import io.github.zlatozar.repair.department.internal.domain.Department;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent.RepairBoxApproved;
import io.github.zlatozar.repair.department.internal.domain.DepartmentException.UnknownDepartment;
import io.github.zlatozar.repair.department.internal.domain.Instrument;
import io.github.zlatozar.repair.department.internal.domain.InstrumentState;
import io.github.zlatozar.repair.department.internal.domain.Ownership.Ours;
import io.github.zlatozar.repair.department.internal.domain.RepairBoxId;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The use cases run without Spring, HTTP or a database: a map and a list replace the adapters. */
class DepartmentServiceTest {

  private static final DepartmentCode SURGERY = new DepartmentCode("SURGERY");
  private static final InstrumentId SCALPEL = new InstrumentId("SC-1042");

  private final InMemoryDepartments departments = new InMemoryDepartments();
  private final List<DepartmentEvent> published = new ArrayList<>();
  private final DepartmentService service = new DepartmentService(departments, published::add);

  @Test
  void publishesWhatTheDepartmentDecided() {
    departments.save(
        new Department(
            SURGERY,
            List.of(new Instrument(SCALPEL, "Scalpel", new Ours(), InstrumentState.USABLE)),
            List.of()));

    service.sendForRepair(
        new SendForRepairCommand(SURGERY, SCALPEL, new Damage(Damage.Kind.BLUNT, "Does not cut")));
    service.approveRepairBox(new ApproveRepairBoxCommand(SURGERY, new RepairBoxId(1)));

    assertThat(published).singleElement().isInstanceOf(RepairBoxApproved.class);
    assertThat(service.view(SURGERY).openBoxes()).isEmpty();
  }

  @Test
  void refusesAnUnknownDepartment() {
    assertThatThrownBy(() -> service.view(SURGERY))
        .isInstanceOf(UnknownDepartment.class)
        .hasMessage("There is no department SURGERY");
  }

  private static final class InMemoryDepartments implements DepartmentRepository {

    private final Map<DepartmentCode, Department> stored = new HashMap<>();

    @Override
    public Optional<Department> findByCode(DepartmentCode code) {
      return Optional.ofNullable(stored.get(code));
    }

    @Override
    public Optional<Department> findByCodeForUpdate(DepartmentCode code) {
      return findByCode(code);
    }

    @Override
    public void save(Department department) {
      stored.put(department.code(), department);
    }
  }
}
