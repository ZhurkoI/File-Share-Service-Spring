package org.zhurko.fileshareservicespring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.zhurko.fileshareservicespring.entity.File;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.security.jwt.JwtUser;
import org.zhurko.fileshareservicespring.security.jwt.JwtUserFactory;
import org.zhurko.fileshareservicespring.service.FileService;
import org.zhurko.fileshareservicespring.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest()
public class FileServiceTest {
    @Autowired
    @SpyBean
    FileService fileService;

    @Autowired
    UserService userService;

    @Sql({"/fileServiceTest-init-data.sql"})
    @Test
    void givenFilesInDb_whenRegularUserGetsActiveFileById_thenFileIsReturned() {
        User user = userService.getByUsername("user");
        JwtUser jwtUser = JwtUserFactory.create(user);
        when(fileService.getCurrentJwtUser()).thenReturn(jwtUser);
        when(fileService.isCurrentUserNotAdminOrModerator()).thenReturn(true);
        File file = fileService.getById(1L);
        assertEquals(1L, file.getId());
    }

    @Test
    void givenFilesInDb_whenRegularUserGetsDeletedFileById_thenNoSuchElementExceptionIsThrown() {
    }

    @Test
    void givenFilesInDb_whenModeratorGetsActiveFileById_thenFileIsReturned() {
    }

    @Test
    void givenFilesInDb_whenModeratorGetsDeletedFileById_thenFileIsReturned() {
    }

    @Test
    void givenFilesInDb_whenAdministratorGetsActiveFileById_thenFileIsReturned() {
    }

    @Test
    void givenFilesInDb_whenAdministratorGetsDeletedFileById_thenFileIsReturned() {
    }

    @Test
    void givenFilesInDb_whenRegularUserGetsFileWithNonexistentId_thenNoSuchElementExceptionIsThrown() {
    }

    @Test
    void givenFilesInDb_whenRegularUserRequestsItsOwnFile_thenFileIsReturned() {
    }

    @Test
    void givenFilesInDb_whenRegularUserRequestsFileOfAnotherUser_henNoSuchElementExceptionIsThrown() {
    }

    @Test
    void givenFilesInDb_whenModeratorRequestsFileOfAnotherUser_thenFileIsReturned() {
    }

    @Test
    void givenFilesInDb_whenAdministratorRequestsFileOfAnotherUser_thenFileIsReturned() {
    }
    /////////////////////////////////////
}
