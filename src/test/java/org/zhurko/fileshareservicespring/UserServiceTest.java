package org.zhurko.fileshareservicespring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.entity.UserRole;
import org.zhurko.fileshareservicespring.repository.RoleRepository;
import org.zhurko.fileshareservicespring.service.UserService;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepo;

    private final User newUser = new User(
            "John",
            "Doe",
            "john-doe",
            "john-doe@test.com",
            "testPass"
    );

    private final User defaultAdmin = new User(
            "admin",
            "admin",
            "admin",
            "admin@test.com",
            "test"
    );

    @Test
    void givenInitialDatabase_whenApplicationStarts_thenAdminUserExistsInDb() {
        String expectedUsername = defaultAdmin.getUsername();

        User user = userService.getByUsername(expectedUsername);

        assumeTrue(user != null);
        assertEquals(expectedUsername, user.getUsername());
    }

    @Transactional
    @Test
    void givenInitialDatabase_whenRegisterNewUser_thenUserIsSavedWithCorrectRoleAndStatus() {
        User savedUser = userService.register(newUser);

        assertEquals(Status.ACTIVE, savedUser.getStatus());
        assertEquals(1, savedUser.getRoles().size());
        assertEquals("ROLE_USER", savedUser.getRoles().get(0).getName());
    }

    @Test
    void givenInitialDatabase_whenCallingGetAll_thenAllUsersAreFetchedFromDb() {
        // test user inserted via init script
        User userJohnDoe0 = new User();
        userJohnDoe0.setFirstName("John-0");
        userJohnDoe0.setLastName("Doe-0");
        userJohnDoe0.setEmail("john-doe-0@test.com");
        userJohnDoe0.setUsername("john-doe-0");

        List<User> result = userService.getAll();
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(defaultAdmin, userJohnDoe0)));
    }

    @Test
    void givenInitialDatabase_whenSearchForUserWithExistentId_thenCorrectUserIsReturned() {
        Long userId = 1L;
        User user = userService.getById(userId);

        assertEquals(userId, user.getId());
        assertEquals(defaultAdmin, user);
    }

    @Test
    void givenInitialDatabase_whenSearchForUserWithNonExistentId_thenNoSuchElementExceptionIsThrown() {
        Long userId = 100500L;

        assertThrows(NoSuchElementException.class, () -> {
            userService.getById(userId);
        });
    }

    @Test
    void givenInitialDatabase_whenSearchingExistentUserByUsername_thenCorrectUserIsReturned() {
        String username = "john-doe-0";   // the user inserted by init script
        User user = userService.getByUsername(username);

        assertEquals(username, user.getUsername());
    }

    @Test
    void givenInitialDatabase_whenTryingToFindNonExistentUserByUsername_henNoSuchElementExceptionIsThrown() {
        String nonExistentUsername = "nonExistentUser";

        assertThrows(NoSuchElementException.class, () -> {
            userService.getByUsername(nonExistentUsername);
        });
    }

    @Test
    void givenUserInDb_whenCallingDeleteUserByIdMethod_thenStatusAndTimestampAreUpdatedForTheUser() {
        String username = "john-doe-0";   // the user inserted by init script
        User existentUser = userService.getByUsername(username);
        userService.deleteById(existentUser.getId());
        User deletedUser = userService.getById(existentUser.getId());

        assertEquals(Status.DELETED, deletedUser.getStatus());
        assertTrue(existentUser.getUpdated().before(deletedUser.getUpdated()));
    }

    @Test
    void givenInitialDatabase_whenTryingToDeleteNonexistentUser_thenNoSuchElementExceptionIsThrown() {
        Long nonExistentUserId = 100500L;

        assertThrows(NoSuchElementException.class, () -> {
            userService.deleteById(nonExistentUserId);
        });
    }

    @Transactional
    @Test
    void givenUserInDb_whenAddingNewRoleToUser_thenRolesAndTimestampAreUpdated() {
        User user = userService.register(newUser);
        Date initialTimestamp = user.getUpdated();

        UserRole moderatorRole = roleRepo.findByName("ROLE_MODERATOR");
        List<UserRole> roles = user.getRoles();
        roles.add(moderatorRole);
        user.setRoles(roles);

        User updatedUser = userService.update(user);
        Date updatedTimestamp = updatedUser.getUpdated();
        List<String> roleNames = updatedUser.getRoles().stream().map(UserRole::getName).collect(Collectors.toList());

        assertTrue(roleNames.containsAll(List.of("ROLE_MODERATOR", "ROLE_USER")));
        assertTrue(initialTimestamp.before(updatedTimestamp));
    }

    @Transactional
    @Test
    void givenUserInDb_whenChangingUserStatus_thenStatusAndTimestampAreUpdated() {
        User user = userService.register(newUser);
        Date initialTimestamp = user.getUpdated();

        user.setStatus(Status.DELETED);
        User updatedUser = userService.update(user);
        Date updatedTimestamp = updatedUser.getUpdated();

        assertEquals(Status.DELETED, updatedUser.getStatus());
        assertTrue(initialTimestamp.before(updatedTimestamp));
    }

    @Transactional
    @Test
    void givenUserInDb_whenTryingToUpdateNonExistentUser_thenNoSuchElementExceptionIsThrown() {
        User userToUpdated = userService.register(newUser);
        Long nonExistentUserId = 100500L;
        userToUpdated.setId(nonExistentUserId);

        assertThrows(NoSuchElementException.class, () -> {
            userService.update(userToUpdated);
        });
    }
}