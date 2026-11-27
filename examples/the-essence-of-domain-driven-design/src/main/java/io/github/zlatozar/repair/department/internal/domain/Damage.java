package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/** What the nurse saw: the kind of damage and a short note for the technician. */
@ValueObject
public record Damage(Kind kind, String note) {

  public static final int MAX_NOTE_LENGTH = 200;

  public enum Kind {
    BLUNT,
    BENT,
    BROKEN
  }

  public Damage {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(note, "note");
    if (note.isBlank() || note.length() > MAX_NOTE_LENGTH) {
      throw new InvalidValueException(
          "A damage note needs 1 to " + MAX_NOTE_LENGTH + " characters");
    }
  }
}
