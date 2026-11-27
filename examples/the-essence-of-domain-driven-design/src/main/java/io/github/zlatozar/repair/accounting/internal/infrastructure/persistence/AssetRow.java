package io.github.zlatozar.repair.accounting.internal.infrastructure.persistence;

import static java.util.stream.Collectors.toMap;

import io.github.zlatozar.repair.accounting.internal.domain.Asset;
import io.github.zlatozar.repair.accounting.internal.domain.AssetState;
import io.github.zlatozar.repair.accounting.internal.domain.Repair;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.sharedkernel.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

/**
 * How an asset is stored: one row in {@code asset}, and one row in {@code asset_repair} for each
 * repair. The repair cost is not stored, because the asset calculates it from its repairs.
 */
@Table("asset")
record AssetRow(
    @Id String instrument,
    BigDecimal priceAmount,
    String priceCurrency,
    AssetState state,
    @MappedCollection(idColumn = "asset", keyColumn = "job") Map<String, RepairRow> repairs) {

  static AssetRow of(Asset asset) {
    Money price = asset.purchasePrice();
    return new AssetRow(
        asset.id().value(),
        price.amount(),
        price.currency().getCurrencyCode(),
        asset.state(),
        asset.repairs().stream().collect(toMap(Repair::job, RepairRow::of)));
  }

  /** Sorted by job, so an asset reads the same after every load. */
  Asset toDomain() {
    return new Asset(
        new InstrumentId(instrument),
        new Money(priceAmount, Currency.getInstance(priceCurrency)),
        repairs.entrySet().stream()
            .sorted(Entry.comparingByKey())
            .map(entry -> entry.getValue().toDomain(entry.getKey()))
            .toList(),
        state);
  }

  @Table("asset_repair")
  record RepairRow(BigDecimal costAmount, String costCurrency) {

    static RepairRow of(Repair repair) {
      return new RepairRow(repair.cost().amount(), repair.cost().currency().getCurrencyCode());
    }

    Repair toDomain(String job) {
      return new Repair(job, new Money(costAmount, Currency.getInstance(costCurrency)));
    }
  }
}
