package io.github.zlatozar.repair;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

/** The API reference shows the contracts the build copies from api/, not a copy kept by hand. */
@SpringBootTest
class ApiReferenceIT {

  private MockMvcTester browser;

  @BeforeEach
  void openTheApplication(WebApplicationContext context) {
    browser = MockMvcTester.from(context);
  }

  @Test
  void onePageListsTheThreeContracts() {
    assertThat(browser.get().uri("/scalar"))
        .hasStatusOk()
        .bodyText()
        .contains("/openapi/department.yaml", "/openapi/workshop.yaml", "/openapi/accounting.yaml");
  }

  @ParameterizedTest
  @ValueSource(strings = {"department", "workshop", "accounting"})
  void eachContractIsServed(String context) {
    assertThat(browser.get().uri("/openapi/{context}.yaml", context))
        .hasStatusOk()
        .bodyText()
        .startsWith("openapi: 3.0.3");
  }
}
