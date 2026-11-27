package io.github.zlatozar.repair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * A modular monolith: each direct subpackage is one Spring Modulith module.
 *
 * <p>{@code @EnableAsync} keeps the modules independent at runtime too, not only in the code. When
 * the department approves a box, it announces this with an event, and its work is done. The
 * workshop handles the event later, on its own. If the workshop is slow or fails, the department
 * does not notice.
 */
@SpringBootApplication
@EnableAsync
public class InstrumentRepairApplication {

  public static void main(String[] args) {
    SpringApplication.run(InstrumentRepairApplication.class, args);
  }
}
