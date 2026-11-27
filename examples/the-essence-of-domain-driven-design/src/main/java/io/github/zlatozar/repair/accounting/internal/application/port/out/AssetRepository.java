package io.github.zlatozar.repair.accounting.internal.application.port.out;

import io.github.zlatozar.repair.accounting.internal.domain.Asset;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;

/**
 * Loads and stores assets. Assets are bought, not created here: the hospital's records bring them
 * in, so there is no {@code add}.
 */
@Repository
public interface AssetRepository {

  /** For reading only: takes no lock. */
  Optional<Asset> findById(InstrumentId id);

  /** For a change: locks the asset until the transaction ends. */
  Optional<Asset> findByIdForUpdate(InstrumentId id);

  /** Stores the changes of an asset loaded for update. */
  void save(Asset asset);
}
