package io.github.zlatozar.repair.workshop;

import static io.github.zlatozar.repair.ContractCheck.client;
import static io.github.zlatozar.repair.ContractCheck.contract;
import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import io.github.zlatozar.repair.sharedkernel.InstrumentId;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxCommand;
import io.github.zlatozar.repair.workshop.internal.application.port.in.ReceiveRepairBoxUseCase;
import io.github.zlatozar.repair.workshop.internal.domain.Damage;
import io.github.zlatozar.repair.workshop.internal.domain.RepairBox;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Calls the workshop API as a client does; every exchange is checked against {@code
 * api/workshop.yaml}. The box arrives through the use case: this test is about the API, not about
 * the department.
 */
@SpringBootTest
@Transactional
class WorkshopApiIT {

  private static final OpenApiInteractionValidator CONTRACT = contract("api/workshop.yaml");

  private static final String JOB = "SURGERY-1-SC-1042";
  private static final String REPAIRED =
      """
      {"result": "REPAIRED", "cost": {"amount": "45.5", "currency": "EUR"}}
      """;

  @Autowired ReceiveRepairBoxUseCase receiveRepairBox;

  private MockMvcTester api;

  @BeforeEach
  void receiveABox(WebApplicationContext context) {
    api = client(context, CONTRACT);
    receiveRepairBox.receiveRepairBox(
        new ReceiveRepairBoxCommand(
            new RepairBox(
                "SURGERY",
                1,
                List.of(
                    new RepairBox.Item(
                        new InstrumentId("SC-1042"),
                        new Damage(Damage.Kind.BLUNT, "Does not cut"))))));
  }

  @Test
  void aRepairedJobLeavesTheOpenList() {
    assertThat(api.get().uri("/repair-jobs"))
        .bodyJson()
        .extractingPath("$.jobs[*].id")
        .asArray()
        .contains(JOB);

    assertThat(api.post().uri("/repair-jobs/{id}/start", JOB)).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(outcome(REPAIRED)).hasStatus(HttpStatus.NO_CONTENT);

    assertThat(api.get().uri("/repair-jobs/{id}", JOB))
        .hasStatusOk()
        .bodyJson()
        .isLenientlyEqualTo(
            """
            {"state": "FINISHED",
             "outcome": {"result": "REPAIRED", "cost": {"amount": "45.50", "currency": "EUR"}}}
            """);
    assertThat(api.get().uri("/repair-jobs"))
        .bodyJson()
        .extractingPath("$.jobs[*].id")
        .asArray()
        .doesNotContain(JOB);
  }

  @Test
  void aRepairFinishesOnlyAfterItStarts() {
    assertThat(outcome(REPAIRED)).hasStatus(HttpStatus.CONFLICT);
  }

  @Test
  void anUnknownJobIsNotFound() {
    assertThat(api.get().uri("/repair-jobs/SURGERY-9-SC-1042")).hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void lexicalCheckRefusesABadlyWrittenId() {
    assertThat(api.get().uri("/repair-jobs/surgery-1")).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void syntaxCheckNeedsTheKindOfOutcome() {
    assertThat(
            outcome(
                """
                {"cost": {"amount": "45.50", "currency": "EUR"}}
                """))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void semanticCheckRefusesCentsInYen() {
    api.post().uri("/repair-jobs/{id}/start", JOB).exchange();

    assertThat(
            outcome(
                """
                {"result": "REPAIRED", "cost": {"amount": "100.5", "currency": "JPY"}}
                """))
        .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
  }

  private MockMvcTester.MockMvcRequestBuilder outcome(String json) {
    return api.post()
        .uri("/repair-jobs/{id}/outcome", JOB)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);
  }
}
