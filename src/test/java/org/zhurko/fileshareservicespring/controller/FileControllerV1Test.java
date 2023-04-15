package org.zhurko.fileshareservicespring.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zhurko.fileshareservicespring.entity.File;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.service.AmazonS3Service;
import org.zhurko.fileshareservicespring.service.FileService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/insert-users.sql")
class FileControllerV1Test {

    private static final String FILE_ENDPOINT = "/api/v1/files/";
    private static int fileCounter = 0;
    private final String fooFileName = "hello.txt";
    private final String fooFileContent = "Hello, World!";
    private final String fooRegularUser1 = "regular-johndoe1";
    private final String fooRegularUser2 = "regular-johndoe2";
    private final String fooModerator = "moderator";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FileService fileService;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @MockBean
    private AmazonS3Service amazonS3Service;

    @Test
    @Transactional
    @WithMockUser(username = fooRegularUser1, roles = {"USER"})
    void whenUserUploadsAFile_thenFileIsUploaded() throws Exception {
        MockMultipartFile testMultipartFile = getMockMultipartFile(fooFileName, fooFileContent);

        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(multipart(FILE_ENDPOINT + "upload").file(testMultipartFile))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").exists(),
                        jsonPath("$.path").value(String.format("%s/%s", bucketName, fooRegularUser1)),
                        jsonPath("$.fileName").value(fooFileName)
                );
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    @WithMockUser(username = fooModerator, roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminRequestsExistingFileById_thenFileIsReturnedWithItsStatus() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);
        File file = uploadFileViaFileService(testFileName, fooFileContent);

        mvc.perform(get(FILE_ENDPOINT + file.getId()))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").exists(),
                        jsonPath("$.path").value(String.format("%s/%s", bucketName, fooModerator)),
                        jsonPath("$.fileName").value(testFileName),
                        jsonPath("$.status").value(Status.ACTIVE.name())
                );
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    void whenModeratorRequestsAFileOfAnotherUser_thenFileIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);
        MockMultipartFile testFile = getMockMultipartFile(testFileName, fooFileContent);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        MvcResult result = mockMvc.perform(multipart(FILE_ENDPOINT + "upload")
                        .file(testFile)
                        .with(user(fooRegularUser1).roles("USER")))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
        Integer fileId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mvc.perform(get(FILE_ENDPOINT + fileId).with(user(fooModerator).roles("MODERATOR")))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.fileName").value(testFileName)
                );
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    void whenAdminRequestsAFileOfAnotherUser_thenFileIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);
        MockMultipartFile testFile = getMockMultipartFile(testFileName, fooFileContent);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        MvcResult result = mockMvc.perform(multipart(FILE_ENDPOINT + "upload")
                        .file(testFile)
                        .with(user(fooRegularUser1).roles("USER")))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
        Integer fileId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mvc.perform(get(FILE_ENDPOINT + fileId).with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.fileName").value(testFileName)
                );
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    @WithMockUser(username = fooRegularUser1, roles = {"USER"})
    void whenRegularUserRequestsExistingFileById_thenFileIsReturnedWithoutItsStatus() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);
        File file = uploadFileViaFileService(testFileName, fooFileContent);

        mvc.perform(get(FILE_ENDPOINT + file.getId()))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").exists(),
                        jsonPath("$.path").value(String.format("%s/%s", bucketName, fooRegularUser1)),
                        jsonPath("$.fileName").value(testFileName),
                        jsonPath("$.status").doesNotExist()
                );
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    @WithMockUser(username = fooRegularUser1, roles = {"USER"})
    void whenRegularUserRequestsDeletedFileById_thenNotFoundIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);
        File file = uploadFileViaFileService(testFileName, fooFileContent);
        fileService.deleteById(file.getId());

        mvc.perform(get(FILE_ENDPOINT + file.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
//    @Sql(scripts = "/insert-users.sql")
    @Transactional
    void whenRegularUserRequestsFileOfAnotherUser_thenNotFoundIsReturned() throws Exception {
        MockMultipartFile testMultipartFile = getMockMultipartFile(fooFileName, fooFileContent);

        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        MvcResult result = mockMvc.perform(multipart(FILE_ENDPOINT + "upload")
                        .file(testMultipartFile)
                        .with(user(fooRegularUser1).roles("USER")))
                .andExpect(status().isOk()).andReturn();
        Integer id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mvc.perform(get(FILE_ENDPOINT + id).with(user(fooRegularUser2).roles("USER")))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    void whenRegularUserRequestsAllFiles_thenListOfOnlyActiveFilesOfThisUserIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);

        MockMultipartFile testFile1 = getMockMultipartFile(generateFileName(fooFileName), fooFileContent);
        MockMultipartFile testFile2 = getMockMultipartFile(testFileName, fooFileContent);
        MockMultipartFile testFile3 = getMockMultipartFile(generateFileName(fooFileName), fooFileContent);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        MvcResult result = mockMvc.perform(multipart(FILE_ENDPOINT + "upload")
                        .file(testFile1)
                        .with(user(fooRegularUser1).roles("USER")))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
        Integer fileId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        fileService.deleteById(Long.valueOf(fileId));

        mockMvc.perform(multipart(FILE_ENDPOINT + "upload")
                        .file(testFile2)
                        .with(user(fooRegularUser1).roles("USER")))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(multipart(FILE_ENDPOINT + "upload")
                        .file(testFile3)
                        .with(user(fooRegularUser2).roles("USER")))
                .andDo(print())
                .andExpect(status().isOk());

        mvc.perform(get(FILE_ENDPOINT).with(user(fooRegularUser1).roles("USER")))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$.[0].fileName").value(testFileName)
                );
    }

    @Test
    @Transactional
    @WithMockUser(username = fooRegularUser1, roles = {"USER"})
    void whenRegularUserTriesToDeleteAFile_thenNoAccessIsReturned() throws Exception {
        mvc.perform(delete(FILE_ENDPOINT + 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    @WithMockUser(username = fooModerator, roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminTriesToDeleteAFile_thenFileStatusAndUpdatedTimestampAreChanged() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);
        File file = uploadFileViaFileService(testFileName, fooFileContent);
        Date initUpdatedTimestamp = file.getUpdated();

        mvc.perform(delete(FILE_ENDPOINT + file.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        File deletedFile = fileService.getById(file.getId());

        assertTrue(deletedFile.getUpdated().after(initUpdatedTimestamp));
        assertEquals(Status.DELETED, deletedFile.getStatus());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"USER"})
    void whenRegularTriesToUpdateAFile_thenNoAccessIsReturned() throws Exception {
        String requestBody = "\"key\": \"value\"";

        mvc.perform(put(FILE_ENDPOINT)
                        .contentType(requestBody)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
//    @Sql(scripts = "/insert-users.sql")
    @WithMockUser(username = fooModerator, roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminTriesToUpdateAFile_thenFileStatusAndUpdatedTimestampAreChanged() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");

        String testFileName = generateFileName(fooFileName);
        File file = uploadFileViaFileService(testFileName, fooFileContent);
        Date initUpdatedTimestamp = file.getUpdated();

        String requestBody = String.format("{\"id\": \"%s\", " +
                "\"path\": \"%s\", " +
                "\"fileName\": \"%s\", " +
                "\"status\": \"%s\"}", file.getId(), bucketName + "/" + fooModerator, testFileName, Status.DELETED);

        mvc.perform(put(FILE_ENDPOINT)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.status").value(Status.DELETED.name())
                );

        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        File deletedFile = fileService.getById(file.getId());

        assertTrue(deletedFile.getUpdated().after(initUpdatedTimestamp));
    }

    private File uploadFileViaFileService(String fileName, String fileContent) throws IOException {
        MockMultipartFile testMultipartFile = new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileContent.getBytes()
        );

        return fileService.upload(testMultipartFile);
    }

    private String generateFileName(String prefix) {
        fileCounter++;
        return String.format("%s-%d", prefix, fileCounter);
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String fileContent) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileContent.getBytes()
        );
    }
}