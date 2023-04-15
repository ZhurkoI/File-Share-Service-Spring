package org.zhurko.fileshareservicespring.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.entity.File;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.repository.EventRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest()
class EventServiceTest {

    @Autowired
    EventService eventService;

    @MockBean
    EventRepository eventRepository;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsActiveEventById_thenEventIsReturned() {
        User user = new User();
        user.setUsername(getUsernameOfCurrentPrincipal());

        Long eventId = 100500L;
        Event storedEvent = new Event();
        storedEvent.setStatus(Status.ACTIVE);
        storedEvent.setUser(user);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(storedEvent));

        Event actualEvent = eventService.getById(eventId);

        verify(eventRepository, times(1)).findById(eventId);
        assertEquals(Status.ACTIVE, actualEvent.getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsDeletedEventById_thenNoSuchElementExceptionIsThrown() {
        User user = new User();
        user.setUsername(getUsernameOfCurrentPrincipal());

        Long eventId = 100500L;
        Event storedEvent = new Event();
        storedEvent.setStatus(Status.DELETED);
        storedEvent.setUser(user);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(storedEvent));

        assertThrows(NoSuchElementException.class, () -> eventService.getById(eventId));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsActiveEventOfAnotherUser_thenNoSuchElementExceptionIsThrown() {
        User user = new User();
        user.setUsername("anotherUser");

        Long eventId = 100500L;
        Event storedEvent = new Event();
        storedEvent.setStatus(Status.ACTIVE);
        storedEvent.setUser(user);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(storedEvent));

        assertThrows(NoSuchElementException.class, () -> eventService.getById(eventId));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsNonExistentEventById_thenNoSuchElementExceptionIsThrown() {
        Long eventId = 100500L;
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> eventService.getById(eventId));
    }

    @Test
    @WithMockUser(username = "moderatorOrAdmin", roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminGetsDeletedEventById_thenEventIsReturned() {
        User user = new User();
        user.setUsername(getUsernameOfCurrentPrincipal());

        Long eventId = 100500L;
        Event storedEvent = new Event();
        storedEvent.setStatus(Status.DELETED);
        storedEvent.setUser(user);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(storedEvent));

        Event actualEvent = eventService.getById(eventId);

        assertEquals(Status.DELETED, actualEvent.getStatus());
    }

    @Test
    @WithMockUser(username = "moderatorOrAdmin", roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminGetsNonExistentEventById_thenNoSuchElementExceptionIsThrown() {
        Long eventId = 100500L;
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> eventService.getById(eventId));
    }

    private static String getUsernameOfCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsAllEvents_thenOnlyActiveEventsAreReturned() {
        Event event1 = new Event();
        Event event2 = new Event();
        Event event3 = new Event();
        event1.setStatus(Status.ACTIVE);
        event2.setStatus(Status.ACTIVE);
        event3.setStatus(Status.DELETED);
        List<Event> stubbedResult = List.of(event1, event2, event3);
        when(eventRepository.findAllByUsername(getUsernameOfCurrentPrincipal())).thenReturn(stubbedResult);

        List<Event> actualResult = eventService.getAll();

        assertEquals(2, actualResult.size());
        assertEquals(0, (int) actualResult.stream()
                .filter(f -> f.getStatus().equals(Status.DELETED))
                .count());
    }

    @Test
    @WithMockUser(username = "moderatorOrAdmin", roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminGetsAllFiles_thenActiveAndDeletedFilesAreReturned() {
        Event event1 = new Event();
        Event event2 = new Event();
        Event event3 = new Event();
        event1.setStatus(Status.ACTIVE);
        event2.setStatus(Status.ACTIVE);
        event3.setStatus(Status.DELETED);
        List<Event> stubbedResult = List.of(event1, event2, event3);
        when(eventRepository.findAll()).thenReturn(stubbedResult);

        List<Event> actualResult = eventService.getAll();

        assertEquals(2, (int) actualResult.stream().filter(f -> f.getStatus().equals(Status.ACTIVE)).count());
        assertEquals(1, (int) actualResult.stream().filter(f -> f.getStatus().equals(Status.DELETED)).count());
    }

    @Test
    void whenUserSavesEvent_thenSaveMethodOfRepositoryHasBeenCalledWithProperArgument() {
        Event eventToSave = new Event();
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        eventService.save(eventToSave);

        verify(eventRepository, times(1)).save(eventToSave);
    }

    @Test
    void whenUserUpdatesExistingEvent_thenSaveMethodOfRepositoryHasBeenCalledWithProperArgument() {
        User user = new User();
        File file = new File();
        Event eventToUpdate = new Event();
        eventToUpdate.setId(1L);
        eventToUpdate.setFile(file);
        eventToUpdate.setUser(user);

        Event storedEvent = new Event();
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(storedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        eventService.update(eventToUpdate);

        verify(eventRepository, times(1)).save(storedEvent);
    }

    @Test
    void whenUserUpdatesNonexistentEvent_thenNoSuchElementExceptionIsThrown() {
        User user = new User();
        File file = new File();
        Event eventToUpdate = new Event();
        eventToUpdate.setId(1L);
        eventToUpdate.setFile(file);
        eventToUpdate.setUser(user);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> eventService.update(eventToUpdate));
    }

    @Test
    void whenUserTriesToDeleteExistingEvent_thenEventStatusUpdated() {
        Event storedEvent = new Event();
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(storedEvent));
        when(eventRepository.save(storedEvent)).thenReturn(new Event());

        eventService.deleteById(1L);

        verify(eventRepository, times(1)).save(storedEvent);
        assertEquals(Status.DELETED, storedEvent.getStatus());
    }
}
