package io.github.zlatozar.repair.workshop.internal.application.port.out;

import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent;

/** Tells the other contexts what happened in the workshop. */
public interface WorkshopEventPublisher {

  void publish(WorkshopEvent event);
}
