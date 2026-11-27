package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/** The company that owns a third-party instrument. */
@ValueObject
public record Supplier(String name) {

  public Supplier {
    Objects.requireNonNull(name, "name");
    if (name.isBlank()) {
      throw new InvalidValueException("A supplier needs a name");
    }
  }

  @Override
  public String toString() {
    return name;
  }
}
