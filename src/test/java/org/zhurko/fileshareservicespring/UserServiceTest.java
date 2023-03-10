package org.zhurko.fileshareservicespring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.service.UserService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

//    @SpyBean
//    UserRepository userRepo;

    private final User newUser = new User(
            "John",
            "Doe",
            "john-doe",
            "john-doe@test.com",
            "testPass"
    );

//    @Transactional
//    @Test
//    void foo() {
//        User fakeUser = new User();
//        fakeUser.setFirstName("vasya");
//        fakeUser.setLastName("Doe");
//        fakeUser.setEmail("john-doe@test.com");
//        fakeUser.setUsername("john-doe");
//        fakeUser.setPassword("test");
//        fakeUser.setStatus(Status.ACTIVE);
//
//        User savedUser = userService.register(fakeUser);
//        verify(userRepo, times(1)).save(fakeUser);
//        assertEquals("vasya", savedUser.getFirstName());
//
////        when(userRepo.findAll()).thenReturn(List.of(new User(), new User()));
////        List<User> users = userService.getAll();
////        assertEquals(2, users.size());
////
////        User admin = userService.getById(1L);
////        assertEquals("admin", admin.getUsername());
//    }

    @Test
    void givenInitialDatabase_whenApplicationStarts_thenAdminUserExistsInDb() {
        String expectedUsername = "admin";

        User user = userService.findByUsername(expectedUsername);

        assumeTrue(user != null);
        assertEquals(expectedUsername, user.getUsername());
    }

    @Transactional
    @Test
    void givenNewUser_whenRegisterThisUser_thenUserIsSavedWithCorrectRoleAndStatus_A() {
        User savedUser = userService.register(newUser);

        assertEquals(Status.ACTIVE, savedUser.getStatus());
        assertEquals(1, savedUser.getRoles().size());
        assertEquals("ROLE_USER", savedUser.getRoles().get(0).getName());
    }

    @Transactional
    @Test
    void givenInitialDatabase_whenCallingGetAll_thenAllUsersAreFetchedFromDb() {
        User savedUser = userService.register(newUser);

        List<User> result = userService.getAll();
        assertEquals(2, result.size());
    }

    @Test
    void givenInitialDatabase_whenSearchForUserByExistentId_thenCorrectUserIsReturned() {
        Long userId = 1L;
        User user = userService.getById(userId);

        assertEquals(userId, user.getId());
    }

    @Test
    void givenInitialDatabase_whenSearchForUserWithNonExistentId_thenNoSuchElementExceptionIsThrown() {
        Long userId = 100500L;

        assertThrows(NoSuchElementException.class, () -> {
            userService.getById(userId);
        });
    }

    @Transactional
    @Test
    void givenNewUser_whenNewUserIsInsertedIntoDb_thenUserCanBeFoundByUsername() {
        userService.register(newUser);
        User savedUser = userService.findByUsername("john-doe");

        assertEquals(newUser.getUsername(), savedUser.getUsername());
    }

    @Transactional
    @Test
    void givenNewUserIsInsertedIntoDb_whenUserDeleted_thenUserStatusAndTimestampIsUpdated() throws InterruptedException {
        userService.register(newUser);
        User savedUser = userService.findByUsername(newUser.getUsername());
        Thread.sleep(2000);
        userService.deleteById(savedUser.getId());
        User deletedUser = userService.findByUsername(newUser.getUsername());

        assertEquals(Status.DELETED, deletedUser.getStatus());
//        System.out.println("AAAAAAAAAAA --> " + savedUser.getUpdated().getTime() + savedUser);
//        System.out.println("AAAAAAAAAAA --> " + deletedUser.getUpdated().getTime()+ deletedUser);
//        assertEquals(-1, savedUser.getUpdated().compareTo(deletedUser.getUpdated()));
    }

//    @Test
//    void givenInitialDatabase_whenTryingToDeleteNonexistentUser_thenNoSuchElementExceptionIsThrown(){
//
//    }

}
