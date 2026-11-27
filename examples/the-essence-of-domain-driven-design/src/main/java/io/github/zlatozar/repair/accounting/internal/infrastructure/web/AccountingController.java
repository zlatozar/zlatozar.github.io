package io.github.zlatozar.repair.accounting.internal.infrastructure.web;

import io.github.zlatozar.repair.accounting.internal.application.port.in.ViewAssetQuery;
import io.github.zlatozar.repair.accounting.internal.domain.UnknownAsset;
import io.github.zlatozar.repair.accounting.internal.infrastructure.web.generated.AccountingApi;
import io.github.zlatozar.repair.accounting.internal.infrastructure.web.generated.AssetDto;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * A read model: what someone looks at before deciding, for example whether to buy a new instrument.
 * An asset changes only through workshop events, in {@code WorkshopEventListener}.
 */
@RestController
class AccountingController implements AccountingApi {

  private final ViewAssetQuery viewAsset;
  private final AccountingApiMapper mapper;

  AccountingController(ViewAssetQuery viewAsset, AccountingApiMapper mapper) {
    this.viewAsset = viewAsset;
    this.mapper = mapper;
  }

  @Override
  public AssetDto viewAsset(String instrumentId) {
    return mapper.toDto(viewAsset.view(new InstrumentId(instrumentId)));
  }

  @ExceptionHandler
  ProblemDetail notFound(UnknownAsset unknown) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, unknown.getMessage());
  }
}
