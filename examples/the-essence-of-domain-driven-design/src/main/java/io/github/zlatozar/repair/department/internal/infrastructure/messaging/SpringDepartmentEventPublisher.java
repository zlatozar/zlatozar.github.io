package io.github.zlatozar.repair.department.internal.infrastructure.messaging;

import io.github.zlatozar.repair.department.internal.application.port.out.DepartmentEventPublisher;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent.RepairBoxApproved;
import io.github.zlatozar.repair.department.internal.domain.DepartmentEvent.ReplacementRequested;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** The driven adapter: Spring delivers the public events to the listeners of other modules. */
@Component
class SpringDepartmentEventPublisher implements DepartmentEventPublisher {

  private final ApplicationEventPublisher events;
  private final DepartmentEventMapper mapper;

  SpringDepartmentEventPublisher(ApplicationEventPublisher events, DepartmentEventMapper mapper) {
    this.events = events;
    this.mapper = mapper;
  }

  @Override
  public void publish(DepartmentEvent event) {
    Object published =
        switch (event) {
          case RepairBoxApproved approved -> mapper.toPublic(approved);
          case ReplacementRequested requested -> mapper.toPublic(requested);
        };
    events.publishEvent(published);
  }
}
