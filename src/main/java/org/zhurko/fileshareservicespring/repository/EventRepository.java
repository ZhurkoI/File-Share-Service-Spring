package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.repository.CrudRepository;
import org.zhurko.fileshareservicespring.model.entity.Event;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {

    List<Event> getByUserId(Long id);   // TODO: это Спринг не сможет сделать?
}
