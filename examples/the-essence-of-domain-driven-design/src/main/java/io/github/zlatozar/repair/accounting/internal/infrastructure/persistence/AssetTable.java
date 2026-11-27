package io.github.zlatozar.repair.accounting.internal.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.Repository;

/** Spring Data generates it. Only the methods the adapter needs, nothing more. */
interface AssetTable extends Repository<AssetRow, String> {

  Optional<AssetRow> findByInstrument(String instrument);

  /** SELECT ... FOR UPDATE. Spring Data ignores the words between "find" and "By". */
  @Lock(LockMode.PESSIMISTIC_WRITE)
  Optional<AssetRow> findForUpdateByInstrument(String instrument);

  AssetRow save(AssetRow asset);
}
