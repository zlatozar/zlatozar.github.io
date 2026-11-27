package io.github.zlatozar.repair.workshop.internal.infrastructure.messaging;

import io.github.zlatozar.repair.workshop.internal.application.port.out.WorkshopEventPublisher;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent.InstrumentRepaired;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent.InstrumentWrittenOff;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** The driven adapter: Spring delivers the public events to the listeners of other modules. */
@Component
class SpringWorkshopEventPublisher implements WorkshopEventPublisher {

  private final ApplicationEventPublisher events;
  private final WorkshopEventMapper mapper;

  SpringWorkshopEventPublisher(ApplicationEventPublisher events, WorkshopEventMapper mapper) {
    this.events = events;
    this.mapper = mapper;
  }

  @Override
  public void publish(WorkshopEvent event) {
    Object published =
        switch (event) {
          case InstrumentRepaired repaired -> mapper.toPublic(repaired);
          case InstrumentWrittenOff writtenOff -> mapper.toPublic(writtenOff);
        };
    events.publishEvent(published);
  }
}
