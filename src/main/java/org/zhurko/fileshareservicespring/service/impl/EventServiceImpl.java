package org.zhurko.fileshareservicespring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.repository.EventRepository;
import org.zhurko.fileshareservicespring.security.jwt.JwtUser;
import org.zhurko.fileshareservicespring.service.EventService;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Event getById(Long eventId) {
        if (JwtUser.isCurrentUserNotAdminOrModerator()) {
            Event result = eventRepository.findById(eventId).orElseThrow(NoSuchElementException::new);

            if (result.getStatus().equals(Status.ACTIVE)
                    && (Objects.equals(result.getUser().getId(), JwtUser.getCurrentJwtUser().getId()))) {
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        return eventRepository.findById(eventId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<Event> getAll() {
        if (JwtUser.isCurrentUserNotAdminOrModerator()) {
            List<Event> result = eventRepository.findAllByUserId(JwtUser.getCurrentJwtUser().getId());
            return result.stream()
                    .filter(e -> e.getStatus().equals(Status.ACTIVE))
                    .collect(Collectors.toList());
        }

        return eventRepository.findAll();
    }

    @Override
    public Event update(Event event) {
        Event result = eventRepository.findById(event.getId()).orElseThrow(NoSuchElementException::new);
        result.setStatus(event.getStatus());
        result.setUpdated(new Date());

        return eventRepository.save(result);
    }

    @Override
    public void deleteById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(NoSuchElementException::new);
        event.setStatus(Status.DELETED);
        event.setUpdated(new Date());
        eventRepository.save(event);
    }
}
