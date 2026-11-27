package io.github.zlatozar.repair.workshop.internal.infrastructure.messaging;

import io.github.zlatozar.repair.workshop.events.InstrumentRepaired;
import io.github.zlatozar.repair.workshop.events.InstrumentWrittenOff;
import io.github.zlatozar.repair.workshop.internal.domain.WorkshopEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct writes the code. The build fails when a domain event gets a field that is not mapped
 * here: someone must decide whether other modules should see it.
 */
@Mapper
interface WorkshopEventMapper {

  @Mapping(target = "job", source = "job.value")
  InstrumentRepaired toPublic(WorkshopEvent.InstrumentRepaired event);

  @Mapping(target = "job", source = "job.value")
  InstrumentWrittenOff toPublic(WorkshopEvent.InstrumentWrittenOff event);
}
