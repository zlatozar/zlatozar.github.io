package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jmolecules.ddd.annotation.ValueObject;

/** A natural ID, for example {@code SURGERY}. */
@ValueObject
public record DepartmentCode(String value) {

  private static final Pattern FORMAT = Pattern.compile("[A-Z][A-Z_]{1,19}");

  public DepartmentCode {
    Objects.requireNonNull(value, "value");
    if (!FORMAT.matcher(value).matches()) {
      throw new InvalidValueException("Not a department code: " + value);
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
