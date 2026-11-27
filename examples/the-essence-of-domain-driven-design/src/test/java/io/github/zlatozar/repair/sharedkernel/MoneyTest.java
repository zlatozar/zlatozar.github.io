package io.github.zlatozar.repair.sharedkernel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Shows the value object rules from the post on {@link Money}. */
class MoneyTest {

  @Test
  void equalAttributesMeanEqualMoney() {
    assertThat(Money.of("10", "EUR")).isEqualTo(Money.of("10.00", "EUR"));
  }

  @Test
  void addReturnsNewMoneyAndChangesNothing() {
    Money price = Money.of("100.00", "EUR");

    Money total = price.add(Money.of("20.50", "EUR"));

    assertThat(total).hasToString("120.50 EUR");
    assertThat(price).hasToString("100.00 EUR");
  }

  @Test
  void refusesToMixCurrencies() {
    Money euros = Money.of("1.00", "EUR");
    Money dollars = Money.of("1.00", "USD");

    assertThatThrownBy(() -> euros.add(dollars))
        .isInstanceOf(InvalidValueException.class)
        .hasMessage("Cannot combine 1.00 EUR with 1.00 USD");
  }

  @ParameterizedTest(name = "\"{0}\" \"{1}\"")
  @CsvSource({
    "-1.00, EUR", // negative
    "0.001, EUR", // more decimals than cents
    "ten,   EUR", // not a number
    "10.00, eur", // not an ISO 4217 code
    "10.00, XAU" // gold is not a currency for money
  })
  void invalidMoneyCannotExist(String amount, String currency) {
    assertThatThrownBy(() -> Money.of(amount, currency)).isInstanceOf(InvalidValueException.class);
  }
}
