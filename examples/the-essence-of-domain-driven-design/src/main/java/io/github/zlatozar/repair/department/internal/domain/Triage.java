package io.github.zlatozar.repair.department.internal.domain;

import io.github.zlatozar.repair.department.internal.domain.Ownership.Ours;
import io.github.zlatozar.repair.department.internal.domain.Ownership.ThirdParty;
import org.jmolecules.ddd.annotation.Service;

/** A domain service: decides whether a damaged instrument is repaired or replaced. */
@Service
public final class Triage {

  public sealed interface Decision {}

  public record Repair(RepairCategory category) implements Decision {}

  public record Replace(Supplier supplier) implements Decision {}

  private Triage() {}

  public static Decision classify(Ownership ownership, Damage damage) {
    return switch (ownership) {
      // We do not repair what we do not own; the owner sends a new one
      case ThirdParty(Supplier owner) -> new Replace(owner);
      case Ours() ->
          new Repair(
              switch (damage.kind()) {
                case BLUNT -> RepairCategory.SHARPENING;
                case BENT, BROKEN -> RepairCategory.MECHANICAL;
              });
    };
  }
}
