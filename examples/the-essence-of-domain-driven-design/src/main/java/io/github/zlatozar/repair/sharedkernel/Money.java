package io.github.zlatozar.repair.sharedkernel;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/** An amount in one currency, for example {@code 120.50 EUR}. Never negative: prices and costs. */
@ValueObject
public record Money(BigDecimal amount, Currency currency) {

  public Money {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(currency, "currency");

    int decimals = currency.getDefaultFractionDigits();
    if (decimals < 0) { // XAU (gold) and other pseudo currencies have no cents
      throw new InvalidValueException("Not a currency for money: " + currency);
    }
    if (amount.signum() < 0) {
      throw new InvalidValueException("Money cannot be negative: " + amount);
    }
    if (amount.stripTrailingZeros().scale() > decimals) {
      throw new InvalidValueException(
          "%s allows %d decimals, but the amount is %s".formatted(currency, decimals, amount));
    }
    // BigDecimal.equals() compares the scale too, so without this 10 and 10.00 would differ
    amount = amount.setScale(decimals);
  }

  public static Money of(String amount, String currencyCode) {
    return new Money(parseAmount(amount), parseCurrency(currencyCode));
  }

  public static Money zero(Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public boolean isGreaterThan(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + currency.getCurrencyCode();
  }

  // There are no exchange rates in this model
  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new InvalidValueException("Cannot combine %s with %s".formatted(this, other));
    }
  }

  private static BigDecimal parseAmount(String text) {
    try {
      return new BigDecimal(text);
    } catch (NumberFormatException e) {
      throw new InvalidValueException("Not an amount of money: " + text, e);
    }
  }

  private static Currency parseCurrency(String code) {
    try {
      return Currency.getInstance(code);
    } catch (IllegalArgumentException e) {
      throw new InvalidValueException("Not an ISO 4217 currency code: " + code, e);
    }
  }
}
