/**
 * Events in and out. A department event becomes a command here. The workshop's own events are
 * translated before other modules see them: the domain may change, the published events may not.
 */
@NullMarked
package io.github.zlatozar.repair.workshop.internal.infrastructure.messaging;

import org.jspecify.annotations.NullMarked;
