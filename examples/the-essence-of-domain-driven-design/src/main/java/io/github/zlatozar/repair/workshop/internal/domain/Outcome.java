package io.github.zlatozar.repair.workshop.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import io.github.zlatozar.repair.sharedkernel.Money;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/** How a repair ends: repaired at a cost, or written off for a reason. */
public sealed interface Outcome extends JobState {

  @ValueObject
  record Repaired(Money cost) implements Outcome {

    public Repaired {
      Objects.requireNonNull(cost, "cost");
    }
  }

  @ValueObject
  record WrittenOff(String reason) implements Outcome {

    public static final int MAX_REASON_LENGTH = 200;

    public WrittenOff {
      Objects.requireNonNull(reason, "reason");
      if (reason.isBlank() || reason.length() > MAX_REASON_LENGTH) {
        throw new InvalidValueException(
            "A write-off reason needs 1 to " + MAX_REASON_LENGTH + " characters");
      }
    }
  }
}
