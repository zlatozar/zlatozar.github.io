package io.github.zlatozar.repair.accounting.internal.application.port.in;

import io.github.zlatozar.repair.accounting.internal.domain.Asset;
import io.github.zlatozar.repair.accounting.internal.domain.AssetState;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;

/** A read-only snapshot, so callers cannot change an asset without a use case. */
public record AssetView(
    InstrumentId id,
    Money purchasePrice,
    Money repairCost,
    AssetState state,
    boolean uneconomical) {

  public static AssetView of(Asset asset) {
    return new AssetView(
        asset.id(),
        asset.purchasePrice(),
        asset.repairCost(),
        asset.state(),
        asset.isUneconomical());
  }
}
