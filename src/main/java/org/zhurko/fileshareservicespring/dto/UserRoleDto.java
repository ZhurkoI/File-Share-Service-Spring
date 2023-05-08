package org.zhurko.fileshareservicespring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.zhurko.fileshareservicespring.entity.UserRole;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleDto {

    private Long id;

    private String name;

    private List<UserDto> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }

    public static UserRole toEntity(UserRoleDto userRoleDto) {
        UserRole userRole = new UserRole();
        userRole.setId(userRoleDto.getId());
        userRole.setName(userRoleDto.getName());

        return userRole;
    }

    public static UserRoleDto fromEntity(UserRole role) {
        UserRoleDto userRoleDto = new UserRoleDto();
        userRoleDto.setName(role.getName());

        return userRoleDto;
    }
}
