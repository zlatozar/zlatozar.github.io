package io.github.zlatozar.repair;

import static io.github.zlatozar.repair.ContractCheck.client;
import static io.github.zlatozar.repair.ContractCheck.contract;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

/**
 * One scalpel through all three contexts, through their APIs only. The contexts talk by events
 * after a commit, so this test commits, waits for each step, and closes its application context:
 * the next test class starts with a clean database.
 */
@SpringBootTest
@DirtiesContext
class RepairFlowIT {

  private static final OpenApiInteractionValidator DEPARTMENT = contract("api/department.yaml");
  private static final OpenApiInteractionValidator WORKSHOP = contract("api/workshop.yaml");
  private static final OpenApiInteractionValidator ACCOUNTING = contract("api/accounting.yaml");

  @Test
  void aBluntScalpelIsSharpenedPaidForAndBackInUse(WebApplicationContext context) throws Exception {
    MockMvcTester department = client(context, DEPARTMENT);
    MockMvcTester workshop = client(context, WORKSHOP);
    MockMvcTester accounting = client(context, ACCOUNTING);

    // Department: the nurse reports the damage, and the director approves the box
    assertThat(
            department
                .post()
                .uri("/departments/SURGERY/instruments/SC-1042/repair")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"kind": "BLUNT", "note": "Does not cut"}
                    """))
        .hasStatus(HttpStatus.ACCEPTED);
    String surgery =
        department.get().uri("/departments/SURGERY").exchange().getResponse().getContentAsString();
    int box = JsonPath.read(surgery, "$.openBoxes[0].id");
    assertThat(department.post().uri("/departments/SURGERY/repair-boxes/{box}/approval", box))
        .hasStatus(HttpStatus.ACCEPTED);

    // Workshop: the box becomes a job, and the technician sharpens the scalpel
    String job = "SURGERY-" + box + "-SC-1042";
    await()
        .untilAsserted(
            () -> assertThat(workshop.get().uri("/repair-jobs/{id}", job)).hasStatusOk());
    assertThat(workshop.post().uri("/repair-jobs/{id}/start", job))
        .hasStatus(HttpStatus.NO_CONTENT);
    assertThat(
            workshop
                .post()
                .uri("/repair-jobs/{id}/outcome", job)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"result": "REPAIRED", "cost": {"amount": "45.50", "currency": "EUR"}}
                    """))
        .hasStatus(HttpStatus.NO_CONTENT);

    // Accounting: the repair is added to what the scalpel has cost
    await()
        .untilAsserted(() ->
            assertThat(accounting.get().uri("/assets/SC-1042"))
                .bodyJson()
                .extractingPath("$.repairCost.amount")
                .isEqualTo("45.50"));

    // Department: the scalpel comes back and can be used again
    assertThat(department.post().uri("/departments/SURGERY/instruments/SC-1042/return"))
        .hasStatus(HttpStatus.NO_CONTENT);
    assertThat(department.get().uri("/departments/SURGERY"))
        .bodyJson()
        .extractingPath("$.instruments[?(@.id == 'SC-1042')].state")
        .asArray()
        .containsExactly("USABLE");
  }
}
