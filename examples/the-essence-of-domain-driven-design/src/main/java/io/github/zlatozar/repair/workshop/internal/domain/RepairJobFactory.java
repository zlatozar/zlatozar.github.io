package io.github.zlatozar.repair.workshop.internal.domain;

import io.github.zlatozar.repair.workshop.internal.domain.JobState.Received;
import java.util.List;
import org.jmolecules.ddd.annotation.Factory;

/** The constructor only rebuilds a job; the factory decides how a new one starts. */
@Factory
public final class RepairJobFactory {

  private RepairJobFactory() {}

  /** One received job for each instrument in the box. */
  public static List<RepairJob> jobsFor(RepairBox box) {
    return box.items().stream()
        .map(item ->
            new RepairJob(
                RepairJobId.of(box.department(), box.number(), item.instrument()),
                item.instrument(),
                box.department(),
                item.damage(),
                new Received()))
        .toList();
  }
}
