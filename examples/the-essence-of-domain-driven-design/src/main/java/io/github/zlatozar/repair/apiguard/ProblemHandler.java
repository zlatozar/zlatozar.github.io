package io.github.zlatozar.repair.apiguard;

import io.github.zlatozar.repair.sharedkernel.InvalidValueException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Every error is an RFC 9457 problem. A request that breaks the contract lists all its errors. */
@RestControllerAdvice
class ProblemHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler
  ProblemDetail refused(InvalidValueException invalid) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, invalid.getMessage());
  }

  @Override
  protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException invalid,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    var errors =
        invalid.getParameterValidationResults().stream()
            .flatMap(result ->
                result.getResolvableErrors().stream()
                    .map(error ->
                        (error instanceof FieldError field
                                ? field.getField()
                                : result.getMethodParameter().getParameterName())
                            + ": "
                            + error.getDefaultMessage()))
            .toList();
    if (invalid.isForReturnValue()) {
      // Our own answer breaks the contract: a bug for us to fix, no detail for the client.
      logger.error("The response breaks the contract: " + errors);
    } else {
      invalid.getBody().setProperty("errors", errors);
    }
    return super.handleHandlerMethodValidationException(invalid, headers, status, request);
  }
}
