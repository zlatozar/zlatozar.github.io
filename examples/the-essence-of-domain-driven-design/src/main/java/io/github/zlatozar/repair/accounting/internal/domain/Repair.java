package io.github.zlatozar.repair.accounting.internal.domain;

import io.github.zlatozar.repair.sharedkernel.Money;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * One repair as accounting sees it: how much it cost. The job ID comes from the workshop.
 * Accounting uses the ID only to count each repair once.
 */
@ValueObject
public record Repair(String job, Money cost) {

  public Repair {
    Objects.requireNonNull(job, "job");
    Objects.requireNonNull(cost, "cost");
  }
}
