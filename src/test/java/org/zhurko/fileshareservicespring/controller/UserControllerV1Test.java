package org.zhurko.fileshareservicespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.repository.UserRepository;
import org.zhurko.fileshareservicespring.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerV1Test {

    private static final String USER_ENDPOINT = "/api/v1/users/";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    UserService userService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void whenApplicationStartsWithDefaultSettings_thenAdminUserExistsInDb() throws Exception {
        Long userId = 1L;

        mvc.perform(get(USER_ENDPOINT + "{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"ADMIN"})
    void whenAdminRegistersNewUser_thenUserIsSavedWithRoleUserAndActiveStatus() throws Exception {
        String requestBody = "{" +
                "\"username\": \"user1\", " +
                "\"firstName\": \"John\", " +
                "\"lastName\": \"Doe\", " +
                "\"email\": \"user1@test.com\", " +
                "\"password\": \"test\"" +
                "}";

        mvc.perform(post(USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername("user1");

        assertEquals(Status.ACTIVE, userRepository.findByUsername("user1").getStatus());
        assertEquals(1, user.getRoles().size());
        assertEquals("ROLE_USER", user.getRoles().get(0).getName());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"ADMIN", "MODERATOR"})
    void whenAdminOrModeratorIsCallingGetAllUsers_thenAllUsersInDbAreReturned() throws Exception {
        User user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("johndoe1@test.com");
        user1.setUsername("johndoe1");
        user1.setPassword("pass");
        User user = userService.register(user1);
        userService.deleteById(user.getId());
        List<User> allUsers = userService.getAll();

        mvc.perform(get(USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(allUsers.size())));

        assertThat(userService.getAll()).filteredOn(u -> u.getUsername().equals("admin")).isNotEmpty();
        assertThat(userService.getAll()).filteredOn(u -> u.getUsername().equals("johndoe1")).isNotEmpty();
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MODERATOR"})
    void whenSearchForUserWithExistingId_thenUserIsReturned() throws Exception {
        Long userId = 1L;

        mvc.perform(get(USER_ENDPOINT + "{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"ADMIN", "MODERATOR"})
    void whenAdminOrModeratorSearchesForUserWithDeletedStatus_thenUserIsReturned() throws Exception {
        User user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("johndoe1@test.com");
        user1.setUsername("johndoe1");
        user1.setPassword("pass");
        User user = userService.register(user1);
        userService.deleteById(user.getId());

        mvc.perform(get(USER_ENDPOINT + "{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.DELETED.name()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MODERATOR"})
    void whenSearchForUserWithNonExistentId_thenNotFoundStatusCodeIsReturned() throws Exception {
        Long userId = 100500L;

        mvc.perform(get(USER_ENDPOINT + "{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER", "MODERATOR"})
    void whenUserWithoutAdminRoleTriesToRegisterNewUser_thenForbiddenStatusCodeIsReturned() throws Exception {
        String requestBody = "unusedBody";

        mvc.perform(post(USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER", "MODERATOR"})
    void whenUserWithoutAdminRoleTriesToUpdateUser_thenForbiddenStatusCodeIsReturned() throws Exception {
        String requestBody = "unusedBody";

        mvc.perform(put(USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER", "MODERATOR"})
    void whenUserWithoutAdminRoleTriesToDeleteUser_thenForbiddenStatusCodeIsReturned() throws Exception {
        Long userId = 1L;

        mvc.perform(delete(USER_ENDPOINT, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"ADMIN"})
    void whenUserHasBeenDeleted_thenNoContentStatusIsReturned() throws Exception {
        User user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("johndoe1@test.com");
        user1.setUsername("johndoe1");
        user1.setPassword("pass");
        User user = userService.register(user1);

        mvc.perform(delete(USER_ENDPOINT + "{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"ADMIN"})
    void whenTryingToDeleteNonexistentUser_thenNotFoundStatusIsReturned() throws Exception {
        Long userId = 100500L;

        mvc.perform(delete(USER_ENDPOINT + "{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER", "MODERATOR"})
    void whenRegularUserOrModeratorTriesToUpdateAUser_thenForbiddenStatusIsReturned() throws Exception {
        String requestBody = "fakeBody";

        mvc.perform(put(USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"ADMIN"})
    void whenAdminIsTryingToUpdateUserStatus_thenUserStatusIsUpdated() throws Exception {
        User initialUser = new User();
        initialUser.setFirstName("John");
        initialUser.setLastName("Doe");
        initialUser.setEmail("johndoe1@test.com");
        initialUser.setUsername("johndoe1");
        initialUser.setPassword("pass");
        User savedUser = userService.register(initialUser);
        userService.deleteById(savedUser.getId());

        String requestBody = String.format("{" +
                "\"id\": \"%d\", " +
                "\"username\": \"johndoe1\", " +
                "\"firstName\": \"John\", " +
                "\"lastName\": \"Doe\", " +
                "\"email\": \"johndoe1@test.com\", " +
                "\"password\": \"test\", " +
                "\"status\": \"%s\"" +
                "}", savedUser.getId(), Status.ACTIVE);

        mvc.perform(put(USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.ACTIVE.name()));
    }
}