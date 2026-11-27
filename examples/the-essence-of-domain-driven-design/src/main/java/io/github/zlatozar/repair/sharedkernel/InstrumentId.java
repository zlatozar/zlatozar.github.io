package io.github.zlatozar.repair.sharedkernel;

import java.util.Objects;
import java.util.regex.Pattern;
import org.jmolecules.ddd.annotation.ValueObject;

/** A natural ID: the code engraved on every instrument, for example {@code SC-1042}. */
@ValueObject
public record InstrumentId(String value) {

  private static final Pattern FORMAT = Pattern.compile("[A-Z]{2,4}-[0-9]{1,8}");

  public InstrumentId {
    Objects.requireNonNull(value, "value");
    if (!FORMAT.matcher(value).matches()) {
      throw new InvalidValueException("Not an instrument ID: " + value);
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
