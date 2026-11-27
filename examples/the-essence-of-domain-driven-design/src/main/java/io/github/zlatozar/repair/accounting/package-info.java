/**
 * Accounting: what each instrument cost to buy and to repair. It hears about repairs from the
 * workshop's {@code events}. It knows nothing about departments, boxes or damage.
 */
@ApplicationModule(
    displayName = "Accounting",
    allowedDependencies = {"sharedkernel", "workshop :: events"})
@NullMarked
package io.github.zlatozar.repair.accounting;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
