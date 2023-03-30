package org.zhurko.fileshareservicespring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.repository.EventRepository;
import org.zhurko.fileshareservicespring.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Override
    public Event save(Event event) {
        Date date = new Date();
        event.setCreated(date);
        event.setUpdated(date);

        return eventRepository.save(event);
    }

    @Override
    public Event getById(Long eventId) {
        if (isCurrentUserNotAdminOrModerator()) {
            Event result = eventRepository.findById(eventId).orElseThrow(NoSuchElementException::new);

            if (result.getStatus().equals(Status.ACTIVE)
                    && (Objects.equals(result.getUser().getUsername(), getUsernameOfCurrentPrincipal()))) {
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        return eventRepository.findById(eventId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<Event> getAll() {
        if (isCurrentUserNotAdminOrModerator()) {
            List<Event> result = eventRepository.findAllByUsername(getUsernameOfCurrentPrincipal());
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

    private boolean isCurrentUserNotAdminOrModerator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().noneMatch(a -> (a.getAuthority().equals("ROLE_ADMIN"))
                || (a.getAuthority().equals("ROLE_MODERATOR")))) {
            return true;
        }
        return false;
    }

    private String getUsernameOfCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
