/**
 * The events the workshop sends to other modules, and the only workshop types they may use. They
 * hold simple data, not domain classes. The compiler catches a removed field, but not a changed
 * meaning, so change them with care.
 */
@NamedInterface("events")
@NullMarked
package io.github.zlatozar.repair.workshop.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
