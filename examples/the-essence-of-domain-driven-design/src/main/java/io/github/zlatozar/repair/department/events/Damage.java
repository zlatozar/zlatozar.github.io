package io.github.zlatozar.repair.department.events;

/** What is wrong with an instrument, as the nurse described it. */
public record Damage(Kind kind, String note) {

  public enum Kind {
    BLUNT,
    BENT,
    BROKEN
  }
}
