package io.github.zlatozar.repair;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.docs.Documenter.CanvasOptions;
import org.springframework.modulith.docs.Documenter.DiagramOptions;

/** Each bounded context is a module; the build fails when one reaches into another. */
class ModularityTest {

  private static final ApplicationModules modules =
      ApplicationModules.of(InstrumentRepairApplication.class);

  /**
   * Fails on dependency cycles, on access to another module's {@code internal} package, on
   * dependencies a module does not declare, and on broken jMolecules DDD rules.
   */
  @Test
  void modulesKeepTheirBoundaries() {
    modules.verify();
  }

  /**
   * Not a check: it writes the module documentation to {@code target/spring-modulith-docs}. It runs
   * as a test so that every build refreshes the documentation. The tables (canvases) list internal
   * types too, so the reader sees each module's aggregates and value objects.
   */
  @Test
  void writesModuleDocumentation() {
    new Documenter(modules)
        .writeDocumentation(DiagramOptions.defaults(), CanvasOptions.defaults().revealInternals());
  }
}
