package org.zhurko.fileshareservicespring.service;

import org.zhurko.fileshareservicespring.entity.Event;

import java.util.List;


public interface EventService {

    Event save(Event event);

    Event getById(Long id);

    List<Event> getAll();

    Event update(Event event);

    void deleteById(Long id);
}
