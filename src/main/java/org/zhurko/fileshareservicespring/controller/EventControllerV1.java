package org.zhurko.fileshareservicespring.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhurko.fileshareservicespring.dto.EventDto;
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.service.EventService;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path = "/api/v1/events/")
public class EventControllerV1 {

    private final EventService eventService;

    public EventControllerV1(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable("id") Long eventId) {
        if (eventId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Event event;
        try {
            event = eventService.getById(eventId);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(EventDto.fromEntity(event), HttpStatus.OK);
    }

    @GetMapping(path = "")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<Event> result = eventService.getAll();
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<EventDto> events = result.stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList());

        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PutMapping(path = "")
    public ResponseEntity<EventDto> updateEvent(@RequestBody @Valid EventDto eventDto) {
        if (eventDto.getId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Event event;
        try {
            event = eventService.update(EventDto.toEntity(eventDto));
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(EventDto.fromEntity(event), HttpStatus.OK);
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable("id") Long eventId) {
        if (eventId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            eventService.deleteById(eventId);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
