package io.github.zlatozar.repair.workshop.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * A natural ID: department, box and instrument, for example {@code SURGERY-1-SC-1042}. Events can
 * arrive twice; the same box then gives the same IDs, so no job is created twice.
 */
@ValueObject
public record RepairJobId(String value) {

  private static final Pattern FORMAT =
      Pattern.compile("[A-Z][A-Z_]{1,19}-[1-9][0-9]{0,8}-[A-Z]{2,4}-[0-9]{1,8}");

  public RepairJobId {
    Objects.requireNonNull(value, "value");
    if (!FORMAT.matcher(value).matches()) {
      throw new InvalidValueException("Not a repair job ID: " + value);
    }
  }

  static RepairJobId of(String department, int box, InstrumentId instrument) {
    return new RepairJobId(department + "-" + box + "-" + instrument);
  }

  @Override
  public String toString() {
    return value;
  }
}
