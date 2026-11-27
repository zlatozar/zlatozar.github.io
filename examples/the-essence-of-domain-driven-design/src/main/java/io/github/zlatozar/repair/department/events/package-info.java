/**
 * The events the department sends to other modules, and the only department types they may use.
 * They hold simple data, not domain classes. The compiler catches a removed field, but not a
 * changed meaning, so change them with care.
 */
@NamedInterface("events")
@NullMarked
package io.github.zlatozar.repair.department.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
