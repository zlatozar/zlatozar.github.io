package io.github.zlatozar.repair.department.internal.infrastructure.web;

import io.github.zlatozar.repair.department.internal.application.port.in.DepartmentView;
import io.github.zlatozar.repair.department.internal.domain.Damage;
import io.github.zlatozar.repair.department.internal.domain.DepartmentCode;
import io.github.zlatozar.repair.department.internal.domain.RepairBoxId;
import io.github.zlatozar.repair.department.internal.infrastructure.web.generated.DamageDto;
import io.github.zlatozar.repair.department.internal.infrastructure.web.generated.DepartmentDto;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import org.mapstruct.Mapper;

/** Value objects travel as plain strings and numbers; the build fails on any unmapped field. */
@Mapper
interface DepartmentApiMapper {

  DepartmentDto toDto(DepartmentView view);

  Damage toDamage(DamageDto damage);

  default String text(DepartmentCode code) {
    return code.value();
  }

  default String text(InstrumentId id) {
    return id.value();
  }

  default Integer number(RepairBoxId id) {
    return id.number();
  }
}
