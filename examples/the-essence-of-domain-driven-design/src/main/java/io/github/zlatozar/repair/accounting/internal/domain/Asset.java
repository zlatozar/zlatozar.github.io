package io.github.zlatozar.repair.accounting.internal.domain;

import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * An instrument as accounting sees it: how much the hospital paid for it, and how much its repairs
 * have cost. In the workshop, the same instrument is a repair job with its damage.
 */
@AggregateRoot
public final class Asset {

  @Identity private final InstrumentId id;
  private final Money purchasePrice;
  private final List<Repair> repairs = new ArrayList<>();
  private Money repairCost;
  private AssetState state;

  public Asset(InstrumentId id, Money purchasePrice, List<Repair> repairs, AssetState state) {
    this.id = Objects.requireNonNull(id, "id");
    this.purchasePrice = Objects.requireNonNull(purchasePrice, "purchasePrice");
    this.state = Objects.requireNonNull(state, "state");
    this.repairCost = Money.zero(purchasePrice.currency());
    repairs.forEach(this::recordRepair);
  }

  /**
   * Adds the cost of a repair. The same job twice is counted once, because an event can arrive
   * again. A written-off asset still takes it: the repair was done and must be paid.
   */
  public void recordRepair(Repair repair) {
    Objects.requireNonNull(repair, "repair");
    if (repairs.stream().anyMatch(recorded -> recorded.job().equals(repair.job()))) {
      return;
    }
    repairCost = repairCost.add(repair.cost()); // Fails on another currency, before any change
    repairs.add(repair);
  }

  /** The instrument will not come back. Writing it off again changes nothing. */
  public void writeOff() {
    state = AssetState.WRITTEN_OFF;
  }

  /** The repairs have cost more than the instrument. A hint for the next purchase, not a rule. */
  public boolean isUneconomical() {
    return repairCost.isGreaterThan(purchasePrice);
  }

  public InstrumentId id() {
    return id;
  }

  public Money purchasePrice() {
    return purchasePrice;
  }

  public List<Repair> repairs() {
    return List.copyOf(repairs);
  }

  public Money repairCost() {
    return repairCost;
  }

  public AssetState state() {
    return state;
  }
}
