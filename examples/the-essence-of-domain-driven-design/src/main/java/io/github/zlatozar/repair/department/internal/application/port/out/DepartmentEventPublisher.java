package io.github.zlatozar.repair.department.internal.application.port.out;

import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent;

/** Tells the other contexts what happened in a department. */
public interface DepartmentEventPublisher {

  void publish(DepartmentEvent event);
}
