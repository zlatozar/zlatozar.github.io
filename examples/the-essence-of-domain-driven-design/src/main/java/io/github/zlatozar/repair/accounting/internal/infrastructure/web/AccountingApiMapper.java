package io.github.zlatozar.repair.accounting.internal.infrastructure.web;

import io.github.zlatozar.repair.accounting.internal.application.port.in.AssetView;
import io.github.zlatozar.repair.accounting.internal.infrastructure.web.generated.AssetDto;
import io.github.zlatozar.repair.accounting.internal.infrastructure.web.generated.MoneyDto;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Value objects travel as plain strings; the build fails on any unmapped field. */
@Mapper
interface AccountingApiMapper {

  @Mapping(target = "instrument", source = "id")
  AssetDto toDto(AssetView view);

  default MoneyDto money(Money money) {
    return new MoneyDto(money.amount().toPlainString(), money.currency().getCurrencyCode());
  }

  default String text(InstrumentId id) {
    return id.value();
  }
}
