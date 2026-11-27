package io.github.zlatozar.repair.workshop.internal.infrastructure.web;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import io.github.zlatozar.repair.workshop.internal.application.port.in.RepairJobView;
import io.github.zlatozar.repair.workshop.internal.domain.JobState;
import io.github.zlatozar.repair.workshop.internal.domain.JobState.InRepair;
import io.github.zlatozar.repair.workshop.internal.domain.JobState.Received;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.Repaired;
import io.github.zlatozar.repair.workshop.internal.domain.Outcome.WrittenOff;
import io.github.zlatozar.repair.workshop.internal.domain.RepairJobId;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.JobStateDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.MoneyDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.OutcomeDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.RepairJobDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.RepairedDto;
import io.github.zlatozar.repair.workshop.internal.infrastructure.web.generated.WrittenOffDto;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Value objects travel as plain strings and numbers; the build fails on any unmapped field. The
 * sealed types are mapped by hand: a switch over them breaks the build when a case is added.
 */
@Mapper
interface WorkshopApiMapper {

  @Mapping(target = "outcome", source = "state")
  RepairJobDto toDto(RepairJobView view);

  default JobStateDto state(JobState state) {
    return switch (state) {
      case Received _ -> JobStateDto.RECEIVED;
      case InRepair _ -> JobStateDto.IN_REPAIR;
      case Outcome _ -> JobStateDto.FINISHED;
    };
  }

  default @Nullable OutcomeDto outcome(JobState state) {
    return switch (state) {
      case Received _, InRepair _ -> null;
      case Repaired(Money cost) -> new RepairedDto(RepairedDto.ResultEnum.REPAIRED, money(cost));
      case WrittenOff(String reason) ->
          new WrittenOffDto(WrittenOffDto.ResultEnum.WRITTEN_OFF, reason);
    };
  }

  default Outcome outcome(OutcomeDto outcome) {
    return switch (outcome) {
      case RepairedDto repaired -> new Repaired(money(repaired.getCost()));
      case WrittenOffDto writtenOff -> new WrittenOff(writtenOff.getReason());
    };
  }

  default MoneyDto money(Money money) {
    return new MoneyDto(money.amount().toPlainString(), money.currency().getCurrencyCode());
  }

  default Money money(MoneyDto money) {
    return Money.of(money.getAmount(), money.getCurrency());
  }

  default String text(RepairJobId id) {
    return id.value();
  }

  default String text(InstrumentId id) {
    return id.value();
  }
}
