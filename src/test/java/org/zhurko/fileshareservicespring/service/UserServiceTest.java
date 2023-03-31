package org.zhurko.fileshareservicespring.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.entity.UserRole;
import org.zhurko.fileshareservicespring.repository.RoleRepository;
import org.zhurko.fileshareservicespring.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    RoleRepository roleRepository;

    @MockBean
    BCryptPasswordEncoder passwordEncoder;

    @Test
    void whenUserIsRequestedById_thenCorrectRepositoryMethodIsCalledWithRespectiveArgument() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));

        userService.getById(1L);

        verify(userRepository).findById(1L);
    }

    @Test
    void whenAllUserAreRequested_thenCorrectRepositoryMethodIsCalled() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        userService.getAll();

        verify(userRepository).findAll();
    }

    @Test
    void whenUserIsRequestedByUsername_thenCorrectRepositoryMethodIsCalled() {
        String testUsername = "testUsername";
        when(userRepository.findByUsername(anyString())).thenReturn(new User());

        userService.getByUsername(testUsername);

        verify(userRepository).findByUsername("testUsername");
    }

    @Test
    void whenUserCannotBeFoundByUsername_thenNoSuchElementExceptionIsThrown() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        assertThrows(NoSuchElementException.class, () -> userService.getByUsername("nonExistentUsername"));
    }

    @Test
    void whenTryingToRegisterUser_thenUserIsSavedWithDefaultRoleAndActiveStatus() {
        String defaultRoleName = "ROLE_USER";

        UserRole defaultRole = new UserRole();
        defaultRole.setName(defaultRoleName);

        User userToSave = new User();
        userToSave.setPassword("rawPassword");

        when(roleRepository.findByName(anyString())).thenReturn(defaultRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedPassword");
        when(userRepository.save(userToSave)).thenReturn(new User());

        userService.register(userToSave);

        assertEquals(Status.ACTIVE, userToSave.getStatus());
        assertEquals(1, userToSave.getRoles().size());
        assertEquals(defaultRoleName, userToSave.getRoles().get(0).getName());
    }

    @Test
    void whenTryingToUpdateNonexistentUser_thenNoSuchElementExceptionIsThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.update(new User()));
    }

    @Test
    void whenUserToUpdateHasMoreRoles_thenNewRolesAreAddedToExistingUser() {
        UserRole roleUser = new UserRole();
        roleUser.setName("ROLE_USER");

        UserRole roleAdmin = new UserRole();
        roleAdmin.setName("ROLE_ADMIN");

        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setRoles(List.of(roleUser, roleAdmin));

        User existentUser = new User();
        existentUser.setRoles(List.of(roleUser));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existentUser));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(roleUser);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(roleAdmin);

        userService.update(userToUpdate);

        assertEquals(2, existentUser.getRoles().size());
    }

    @Test
    void whenUserToUpdateHasFewerRoles_thenExtraRolesAreRemovedFromExistingUser() {
        UserRole roleUser = new UserRole();
        roleUser.setName("ROLE_USER");

        UserRole roleAdmin = new UserRole();
        roleAdmin.setName("ROLE_ADMIN");

        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setRoles(List.of(roleUser));

        User existentUser = new User();
        existentUser.setRoles(List.of(roleUser, roleAdmin));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existentUser));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(roleUser);

        userService.update(userToUpdate);

        assertEquals(1, existentUser.getRoles().size());
    }

    @Test
    void whenNewStatusShouldBeSetForUser_thenStatusIsUpdated() {
        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setStatus(Status.ACTIVE);

        User existentUser = new User();
        existentUser.setStatus(Status.DELETED);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existentUser));

        userService.update(userToUpdate);

        assertEquals(Status.ACTIVE, existentUser.getStatus());
    }

    @Test
    void whenTryingToDeleteExistingUser_thenUserSavedWithDeletedStatus() {
        User existingUser = new User();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));

        userService.deleteById(1L);

        assertEquals(Status.DELETED, existingUser.getStatus());
    }

    @Test
    void whenTryingToDeleteNonExistentUser_thenNoSuchElementExceptionIsThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteById(100500L));
    }
}