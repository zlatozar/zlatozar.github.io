package io.github.zlatozar.repair.accounting.internal.application.service;

import io.github.zlatozar.repair.accounting.internal.application.port.in.AssetView;
import io.github.zlatozar.repair.accounting.internal.application.port.in.RecordRepairCommand;
import io.github.zlatozar.repair.accounting.internal.application.port.in.RecordRepairUseCase;
import io.github.zlatozar.repair.accounting.internal.application.port.in.ViewAssetQuery;
import io.github.zlatozar.repair.accounting.internal.application.port.in.WriteOffAssetUseCase;
import io.github.zlatozar.repair.accounting.internal.application.port.in.WriteOffCommand;
import io.github.zlatozar.repair.accounting.internal.application.port.out.AssetRepository;
import io.github.zlatozar.repair.accounting.internal.domain.Asset;
import io.github.zlatozar.repair.accounting.internal.domain.UnknownAsset;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * One transaction per command, and a command changes one asset. Package-private: callers know only
 * the use cases, never this class.
 */
@Service
@Transactional
class AccountingService implements RecordRepairUseCase, WriteOffAssetUseCase, ViewAssetQuery {

  private final AssetRepository assets;

  AccountingService(AssetRepository assets) {
    this.assets = assets;
  }

  @Override
  public void recordRepair(RecordRepairCommand command) {
    Asset asset = load(command.asset());
    asset.recordRepair(command.repair());
    assets.save(asset);
  }

  @Override
  public void writeOff(WriteOffCommand command) {
    Asset asset = load(command.asset());
    asset.writeOff();
    assets.save(asset);
  }

  @Override
  @Transactional(readOnly = true)
  public AssetView view(InstrumentId id) {
    return assets.findById(id).map(AssetView::of).orElseThrow(() -> new UnknownAsset(id));
  }

  // Helper method

  private Asset load(InstrumentId id) {
    return assets.findByIdForUpdate(id).orElseThrow(() -> new UnknownAsset(id));
  }
}
