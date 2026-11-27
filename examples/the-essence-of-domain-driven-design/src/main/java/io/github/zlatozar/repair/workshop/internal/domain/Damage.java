package io.github.zlatozar.repair.workshop.internal.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/** What the department reported. The workshop's own copy: the two contexts change separately. */
@ValueObject
public record Damage(Kind kind, String note) {

  public enum Kind {
    BLUNT,
    BENT,
    BROKEN
  }

  public Damage {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(note, "note");
  }
}
