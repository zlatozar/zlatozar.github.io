/**
 * The hospital department: nurses send broken instruments for repair and the director approves the
 * repair boxes. Other modules see only its {@code events}.
 */
@ApplicationModule(displayName = "Department", allowedDependencies = "sharedkernel")
@NullMarked
package io.github.zlatozar.repair.department;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
