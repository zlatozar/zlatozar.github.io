package io.github.zlatozar.repair.accounting.internal.application.port.in;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;

/** What an instrument has cost the hospital. */
public interface ViewAssetQuery {

  AssetView view(InstrumentId asset);
}
