package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import org.jmolecules.ddd.annotation.ValueObject;

/** A local ID: box 1, box 2, ... unique only inside its department. */
@ValueObject
public record RepairBoxId(int number) {

  public RepairBoxId {
    if (number < 1) {
      throw new InvalidValueException("Box numbers start at 1, not " + number);
    }
  }

  @Override
  public String toString() {
    return String.valueOf(number);
  }
}
