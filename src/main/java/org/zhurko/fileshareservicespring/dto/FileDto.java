package org.zhurko.fileshareservicespring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zhurko.fileshareservicespring.entity.File;
import org.zhurko.fileshareservicespring.entity.Status;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FileDto {

    private Long id;
    private String path;
    private String fileName;
    private Status status;
    private List<EventDto> events = new ArrayList<>();

    public FileDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static File toEntity(FileDto fileDto) {
        File file = new File();
        if (fileDto.getId() != 0) {
            file.setId(fileDto.getId());
        }
        file.setPath(fileDto.getPath());
        file.setFileName(fileDto.getFileName());
        file.setStatus(fileDto.getStatus());

        return file;
    }

    public static FileDto fromEntity(File file) {
        FileDto fileDto = new FileDto();
        fileDto.setId(file.getId());
        fileDto.setPath(file.getPath());
        fileDto.setFileName(file.getFileName());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> (a.getAuthority().equals("ROLE_ADMIN")) || (a.getAuthority().equals("ROLE_MODERATOR")))) {
            fileDto.setStatus(file.getStatus());
        }

        return fileDto;
    }
}
