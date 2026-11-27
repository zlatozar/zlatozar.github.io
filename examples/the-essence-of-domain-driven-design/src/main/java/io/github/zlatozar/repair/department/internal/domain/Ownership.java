package io.github.zlatozar.repair.department.internal.domain;

import org.jmolecules.ddd.annotation.ValueObject;

/** "Instruments are either ours or third-party." */
public sealed interface Ownership {

  @ValueObject
  record Ours() implements Ownership {}

  @ValueObject
  record ThirdParty(Supplier owner) implements Ownership {}
}
