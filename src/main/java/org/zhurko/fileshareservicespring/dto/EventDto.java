package org.zhurko.fileshareservicespring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.entity.EventType;
import org.zhurko.fileshareservicespring.entity.Status;

import javax.validation.constraints.NotNull;
import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {

    private Long id;

    @NotNull
    private EventType eventType;

    @NotNull
    private Status status;

    private FileDto file;
    private UserDto user;
    private Date created;
    private Date updated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public FileDto getFile() {
        return file;
    }

    public void setFile(FileDto file) {
        this.file = file;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static Event toEntity(EventDto eventDto) {
        Event event = new Event();
        if (eventDto.getId() != 0) {
            event.setId(eventDto.getId());
        }
        event.setEventType(eventDto.getEventType());
        event.setStatus(eventDto.getStatus());

        return event;
    }

    public static EventDto fromEntity(Event event) {
        EventDto eventDto = new EventDto();
        eventDto.setId(event.getId());
        eventDto.setEventType(event.getEventType());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> (a.getAuthority().equals("ROLE_ADMIN")) || (a.getAuthority().equals("ROLE_MODERATOR")))) {
            eventDto.setStatus(event.getStatus());
        }

        return eventDto;
    }
}
