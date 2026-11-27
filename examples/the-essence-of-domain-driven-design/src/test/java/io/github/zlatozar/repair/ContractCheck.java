package io.github.zlatozar.repair;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static com.atlassian.oai.validator.whitelist.rule.WhitelistRules.allOf;
import static com.atlassian.oai.validator.whitelist.rule.WhitelistRules.anyOf;
import static com.atlassian.oai.validator.whitelist.rule.WhitelistRules.responseStatusIs;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

/** API tests call the application as a client does, and every exchange must match the contract. */
public final class ContractCheck {

  private ContractCheck() {}

  /**
   * A request that breaks the contract must be refused at the door (400 or 413). If it gets any
   * other answer, it reached the domain, and the test fails. Responses are always checked; that is
   * why the rule reads the message key: {@code WhitelistRules.isRequest()} matches every message.
   */
  public static OpenApiInteractionValidator contract(String spec) {
    return OpenApiInteractionValidator.createFor(spec)
        .withWhitelist(
            ValidationErrorsWhitelist.create()
                .withRule(
                    "Refused at the door",
                    allOf(
                        (message, _, _, _) -> message.getKey().startsWith("validation.request."),
                        anyOf(responseStatusIs(400), responseStatusIs(413)))))
        .build();
  }

  /** A test client that fails on any request or response the contract does not allow. */
  public static MockMvcTester client(
      WebApplicationContext context, OpenApiInteractionValidator contract) {
    return MockMvcTester.from(
        context, mvc -> mvc.alwaysExpect(openApi().isValid(contract)).build());
  }
}
