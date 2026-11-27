package io.github.zlatozar.repair.sharedkernel;

/**
 * Input that cannot become a domain value. Map only this type to an HTTP error; any other {@code
 * IllegalArgumentException} is a bug.
 */
public final class InvalidValueException extends IllegalArgumentException {

  public InvalidValueException(String message) {
    super(message);
  }

  public InvalidValueException(String message, Throwable cause) {
    super(message, cause);
  }
}
