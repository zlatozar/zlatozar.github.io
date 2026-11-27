package io.github.zlatozar.repair.accounting;

import static io.github.zlatozar.repair.ContractCheck.client;
import static io.github.zlatozar.repair.ContractCheck.contract;
import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** Calls the accounting API as a client does; every exchange is checked against the contract. */
@SpringBootTest
@Transactional
class AccountingApiIT {

  private static final OpenApiInteractionValidator CONTRACT = contract("api/accounting.yaml");

  private MockMvcTester api;

  @BeforeEach
  void checkEveryExchangeAgainstTheContract(WebApplicationContext context) {
    api = client(context, CONTRACT);
  }

  @Test
  void anAssetShowsItsPriceAndRepairCost() {
    assertThat(api.get().uri("/assets/SC-1042"))
        .hasStatusOk()
        .bodyJson()
        .isLenientlyEqualTo(
            """
            {"instrument": "SC-1042",
             "purchasePrice": {"amount": "60.00", "currency": "EUR"},
             "repairCost": {"amount": "0.00", "currency": "EUR"},
             "state": "ACTIVE",
             "uneconomical": false}
            """);
  }

  @Test
  void aLeasedInstrumentIsNotAnAsset() {
    assertThat(api.get().uri("/assets/EN-7")).hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void lexicalCheckRefusesABadlyWrittenCode() {
    assertThat(api.get().uri("/assets/sc-1042")).hasStatus(HttpStatus.BAD_REQUEST);
  }
}
