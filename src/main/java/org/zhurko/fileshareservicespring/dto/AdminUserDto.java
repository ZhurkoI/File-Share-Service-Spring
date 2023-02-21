package org.zhurko.fileshareservicespring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserDto {
// TODO: переделать в обычную userDto?
    private Long id;

    @Size(max = 100)
    private String username;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String password;

    private Status status;

    private List<UserRoleDto> roles;

    private List<EventDto> events;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<UserRoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRoleDto> roles) {
        this.roles = roles;
    }

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

    public static User toEntity(AdminUserDto adminUserDto) {
        User user = new User();
        user.setId(adminUserDto.getId());
        user.setFirstName(adminUserDto.getFirstName());
        user.setLastName(adminUserDto.getLastName());
        user.setEmail(adminUserDto.getEmail());
        user.setUsername(adminUserDto.getUsername());
        user.setPassword(adminUserDto.getPassword());
        user.setStatus(adminUserDto.getStatus());
        if (adminUserDto.getEvents() != null) {
            user.setEvents(adminUserDto.getEvents()
                    .stream()
                    .map(EventDto::toEntity)
                    .collect(Collectors.toList()));
        }
        if (adminUserDto.getRoles() != null) {
            user.setRoles(adminUserDto.getRoles()
                    .stream()
                    .map(UserRoleDto::toEntity)
                    .collect(Collectors.toList()));
        }

        return user;
    }

    public static AdminUserDto fromEntity(User user) {
        AdminUserDto adminUserDto = new AdminUserDto();
        adminUserDto.setId(user.getId());
        adminUserDto.setFirstName(user.getFirstName());
        adminUserDto.setLastName(user.getLastName());
        adminUserDto.setEmail(user.getEmail());
        adminUserDto.setUsername(user.getUsername());
        adminUserDto.setStatus(user.getStatus());
        adminUserDto.setRoles(user.getRoles()
                .stream()
                .map(UserRoleDto::fromEntity)
                .collect(Collectors.toList()));
        adminUserDto.setEvents(user.getEvents()
                .stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList()));

        return adminUserDto;
    }
}
