package io.github.zlatozar.repair.workshop.internal.domain;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Where a job is. A finished job holds its {@link Outcome}, so a repaired job without a cost cannot
 * exist.
 *
 * <pre>
 * Received --start--> InRepair --finish--> Repaired or WrittenOff (end states)
 * </pre>
 */
public sealed interface JobState permits JobState.Received, JobState.InRepair, Outcome {

  @ValueObject
  record Received() implements JobState {}

  @ValueObject
  record InRepair() implements JobState {}
}
