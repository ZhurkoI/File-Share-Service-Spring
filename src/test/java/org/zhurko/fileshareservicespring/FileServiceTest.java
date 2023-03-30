package org.zhurko.fileshareservicespring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.entity.File;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.repository.FileRepository;
import org.zhurko.fileshareservicespring.repository.UserRepository;
import org.zhurko.fileshareservicespring.service.AmazonS3Service;
import org.zhurko.fileshareservicespring.service.EventService;
import org.zhurko.fileshareservicespring.service.FileService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
public class FileServiceTest {

    @Autowired
    FileService fileService;

    @MockBean
    EventService eventService;

    @MockBean
    FileRepository fileRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    AmazonS3Service amazonS3Service;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsActiveFileById_thenFileIsReturned() {
        File fooFile = new File();
        fooFile.setStatus(Status.ACTIVE);
        Long fileId = 1L;
        when(fileRepository.findFileOfSpecifiedUser(anyString(), anyLong())).thenReturn(fooFile);

        fileService.getById(fileId);

        verify(fileRepository, times(1)).findFileOfSpecifiedUser(getUsernameOfCurrentPrincipal(), fileId);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsDeletedFileById_thenNoSuchElementExceptionIsThrown() {
        File fooFile = new File();
        fooFile.setStatus(Status.DELETED);
        Long fileId = 1L;
        when(fileRepository.findFileOfSpecifiedUser(anyString(), anyLong())).thenReturn(fooFile);

        assertThrows(NoSuchElementException.class, () -> {
            fileService.getById(fileId);
        });
        verify(fileRepository, times(1)).findFileOfSpecifiedUser(getUsernameOfCurrentPrincipal(), fileId);
    }

    @Test
    @WithMockUser(username = "moderatorOrAdmin", roles = {"MODERATOR", "ADMIN"})
    void whenModeratorGetsActiveFileById_thenFileIsReturned() {
        File fooFile = new File();
        fooFile.setStatus(Status.ACTIVE);
        Long fileId = 1L;
        when(fileRepository.findById(anyLong())).thenReturn(Optional.of(fooFile));

        fileService.getById(fileId);

        verify(fileRepository, times(1)).findById(fileId);
    }

    @Test
    @WithMockUser(username = "moderatorOrAdmin", roles = {"MODERATOR", "ADMIN"})
    void whenModeratorGetsDeletedFileById_thenFileIsReturned() {
        File fooFile = new File();
        fooFile.setStatus(Status.DELETED);
        Long fileId = 1L;
        when(fileRepository.findById(anyLong())).thenReturn(Optional.of(fooFile));

        File actualFile = fileService.getById(fileId);

        assertEquals(Status.DELETED, actualFile.getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsFileWithNonexistentId_thenNoSuchElementExceptionIsThrown() {
        Long fileId = 1L;
        when(fileRepository.findFileOfSpecifiedUser(anyString(), anyLong())).thenReturn(null);

        assertThrows(NoSuchElementException.class, () -> {
            fileService.getById(fileId);
        });
        verify(fileRepository, times(1)).findFileOfSpecifiedUser(getUsernameOfCurrentPrincipal(), fileId);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserGetsAllFiles_thenOnlyActiveFilesAreReturned() {
        File file1 = new File();
        File file2 = new File();
        File file3 = new File();
        file1.setStatus(Status.ACTIVE);
        file2.setStatus(Status.ACTIVE);
        file3.setStatus(Status.DELETED);
        List<File> stubbedResult = List.of(file1, file2, file3);
        when(fileRepository.findAllFilesOfSpecifiedUser(anyString())).thenReturn(stubbedResult);

        List<File> actualResult = fileService.getAll();

        assertEquals(2, actualResult.size());
        assertEquals(0, (int) actualResult.stream()
                .filter(f -> f.getStatus().equals(Status.DELETED))
                .count());
        verify(fileRepository, times(1)).findAllFilesOfSpecifiedUser(anyString());
    }

    @Test
    @WithMockUser(username = "moderatorOrAdmin", roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminGetsAllFiles_thenActiveAndDeletedFilesAreReturned() {
        File file1 = new File();
        File file2 = new File();
        File file3 = new File();
        file1.setStatus(Status.ACTIVE);
        file2.setStatus(Status.ACTIVE);
        file3.setStatus(Status.DELETED);
        List<File> stubbedResult = List.of(file1, file2, file3);
        when(fileRepository.findAll()).thenReturn(stubbedResult);

        List<File> actualResult = fileService.getAll();

        assertEquals(2, (int) actualResult.stream().filter(f -> f.getStatus().equals(Status.ACTIVE)).count());
        assertEquals(1, (int) actualResult.stream().filter(f -> f.getStatus().equals(Status.DELETED)).count());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenRegularUserUploadsFileToApplication_thenFileIsSavedInS3Bucket() throws IOException {
        MultipartFile testMultiPartFile = new MultipartFile() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getOriginalFilename() {
                return "testFilename";
            }

            @Override
            public String getContentType() {
                return "multipart/form-data";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public void transferTo(java.io.File dest) throws IOException, IllegalStateException {

            }
        };
        String testObjectResult = "success";
        when(userRepository.findByUsername(anyString())).thenReturn(new User());
        when(amazonS3Service.upload(anyString(), anyString(), eq(testMultiPartFile.getInputStream()), anyMap()))
                .thenReturn(testObjectResult);
        when(fileRepository.save(any(File.class))).thenReturn(new File());
        when(eventService.save(any(Event.class))).thenReturn(new Event());

        fileService.upload(testMultiPartFile);

        verify(amazonS3Service, times(1)).upload(
                anyString(),
                anyString(),
                eq(testMultiPartFile.getInputStream()),
                anyMap());
        verify(fileRepository, times(1)).save(any(File.class));
        verify(eventService, times(1)).save(any(Event.class));
    }

    @Test
    void whenUserTriesToUpdateFile_thenFileCanBeUpdated() {
        File fileToUpdate = new File();
        fileToUpdate.setId(100500L);
        File storedFile = new File();
        when(fileRepository.findById(anyLong())).thenReturn(Optional.of(storedFile));
        when(fileRepository.save(any(File.class))).thenReturn(new File());

        fileService.update(fileToUpdate);

        verify(fileRepository, times(1)).save(storedFile);
    }

    @Test
    void whenUserTriesToDeleteFile_thenFileCanBeDeleted() {
        File storedFile = new File();
        when(fileRepository.findById(anyLong())).thenReturn(Optional.of(storedFile));
        when(fileRepository.save(storedFile)).thenReturn(new File());

        fileService.deleteById(1L);

        verify(fileRepository, times(1)).save(storedFile);
        assertEquals(Status.DELETED, storedFile.getStatus());
    }

    private static String getUsernameOfCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
