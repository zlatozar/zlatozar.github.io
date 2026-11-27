package io.github.zlatozar.repair.accounting.internal.infrastructure.persistence;

import io.github.zlatozar.repair.accounting.internal.application.port.out.AssetRepository;
import io.github.zlatozar.repair.accounting.internal.domain.Asset;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** The driven adapter: turns the domain into rows and back. */
@Component
class JdbcAssetRepository implements AssetRepository {

  private final AssetTable table;

  JdbcAssetRepository(AssetTable table) {
    this.table = table;
  }

  @Override
  public Optional<Asset> findById(InstrumentId id) {
    return table.findByInstrument(id.value()).map(AssetRow::toDomain);
  }

  @Override
  public Optional<Asset> findByIdForUpdate(InstrumentId id) {
    return table.findForUpdateByInstrument(id.value()).map(AssetRow::toDomain);
  }

  @Override
  public void save(Asset asset) {
    table.save(AssetRow.of(asset));
  }
}
