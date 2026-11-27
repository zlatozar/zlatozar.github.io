/**
 * The workshop: technicians repair the instruments of approved repair boxes. It hears about the
 * boxes from the department's {@code events}; other modules see only its own {@code events}.
 */
@ApplicationModule(
    displayName = "Workshop",
    allowedDependencies = {"sharedkernel", "department :: events"})
@NullMarked
package io.github.zlatozar.repair.workshop;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
