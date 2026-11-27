package io.github.zlatozar.repair.department;

import static io.github.zlatozar.repair.ContractCheck.client;
import static io.github.zlatozar.repair.ContractCheck.contract;
import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Calls the department API as a client does. Every request and every response is checked against
 * {@code api/department.yaml}, so a test fails when the code and the contract disagree.
 *
 * <p>The whole application starts: errors are written by the apiguard module.
 */
@SpringBootTest
@Transactional
class DepartmentApiIT {

  private static final OpenApiInteractionValidator CONTRACT = contract("api/department.yaml");

  private static final String REPAIR = "/departments/SURGERY/instruments/{id}/repair";

  private MockMvcTester api;

  @BeforeEach
  void checkEveryExchangeAgainstTheContract(WebApplicationContext context) {
    api = client(context, CONTRACT);
  }

  @Test
  void aDamagedInstrumentWaitsInARepairBox() {
    assertThat(repair("SC-1042", "BLUNT", "Does not cut")).hasStatus(HttpStatus.ACCEPTED);

    assertThat(api.get().uri("/departments/SURGERY"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.openBoxes[0].category")
        .isEqualTo("SHARPENING");
  }

  @Test
  void sizeCheckRefusesALargeBody() {
    assertThat(repair("SC-1042", "BLUNT", "x".repeat(20_000)))
        .hasStatus(HttpStatus.CONTENT_TOO_LARGE);
  }

  @Test
  void lexicalCheckRefusesABadlyWrittenCode() {
    assertThat(api.get().uri("/departments/surgery")).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void syntaxCheckListsEveryMissingField() {
    assertThat(
            api.post().uri(REPAIR, "SC-1042").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .extractingPath("$.errors")
        .asArray()
        .hasSize(2);
  }

  @Test
  void semanticCheckRefusesANoteOfOnlySpaces() {
    assertThat(repair("SC-1042", "BLUNT", "   ")).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
  }

  @Test
  void anUnknownInstrumentIsNotFound() {
    assertThat(repair("SC-9999", "BLUNT", "Does not cut")).hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void anInstrumentComesBackOnlyFromTheWorkshop() {
    assertThat(repair("SC-1042", "BLUNT", "Does not cut")).hasStatus(HttpStatus.ACCEPTED);

    assertThat(api.post().uri("/departments/SURGERY/instruments/SC-1042/return"))
        .hasStatus(HttpStatus.CONFLICT);
  }

  private MockMvcTester.MockMvcRequestBuilder repair(String instrument, String kind, String note) {
    return api.post()
        .uri(REPAIR, instrument)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"kind": "%s", "note": "%s"}
            """
                .formatted(kind, note));
  }
}
