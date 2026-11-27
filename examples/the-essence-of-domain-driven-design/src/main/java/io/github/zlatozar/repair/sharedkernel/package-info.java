/**
 * The few value objects that all bounded contexts agree on. Keep it small: a change here touches
 * every context. The empty {@code allowedDependencies} forbids dependencies on other modules.
 */
@ApplicationModule(
    displayName = "Shared Kernel",
    allowedDependencies = {})
@NullMarked
package io.github.zlatozar.repair.sharedkernel;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
