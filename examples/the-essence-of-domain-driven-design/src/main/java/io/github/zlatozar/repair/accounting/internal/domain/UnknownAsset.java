package io.github.zlatozar.repair.accounting.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;

/** Accounting has no such instrument. Leased instruments, for example, are not hospital assets. */
public final class UnknownAsset extends RuntimeException {

  public UnknownAsset(InstrumentId instrument) {
    super("There is no asset " + instrument);
  }
}
