package org.zhurko.fileshareservicespring.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.entity.EventType;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.service.AmazonS3Service;
import org.zhurko.fileshareservicespring.service.EventService;

import javax.transaction.Transactional;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/insert-users.sql")
class EventControllerV1Test {

    private static final String EVENT_ENDPOINT = "/api/v1/events/";
    private static final String FILE_ENDPOINT = "/api/v1/files/";
    private static int fileCounter = 0;
    private final String fooFileName = "hello.txt";
    private final String fooFileContent = "Hello, World!";
    private final String fooRegularUser1 = "regular-johndoe1";
    private final String fooRegularUser2 = "regular-johndoe2";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    EventService eventService;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AmazonS3Service amazonS3Service;

    @Test
    @Transactional
    @WithMockUser(username = fooRegularUser1, roles = {"USER"})
    void whenRegularUserRequestsItsOwnEvent_thenEventIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        Integer eventId = uploadTestFile(mockMvc, fooFileName, fooFileContent, fooRegularUser1, "USER");

        mvc.perform(get(EVENT_ENDPOINT + eventId))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(eventId),
                        jsonPath("$.eventType").value(EventType.UPLOADED.name())
                );
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"USER"})
    void whenRegularUserRequestsEventOfAnotherUser_thenNotFoundIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        Integer eventId = uploadTestFile(mockMvc, fooFileName, fooFileContent, fooRegularUser1, "USER");

        mvc.perform(get(EVENT_ENDPOINT + eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminRequestsEventOfAnotherUser_thenEventIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        Integer eventId = uploadTestFile(mockMvc, fooFileName, fooFileContent, fooRegularUser1, "USER");

        mvc.perform(get(EVENT_ENDPOINT + eventId))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(eventId),
                        jsonPath("$.eventType").value(EventType.UPLOADED.name()),
                        jsonPath("$.status").value(Status.ACTIVE.name())
                );
    }

    @Test
    @Transactional
    @WithMockUser(username = fooRegularUser1, roles = {"USER"})
    void whenRegularUserRequestsDeletedEvent_thenNotFoundIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Integer eventId = uploadTestFile(mockMvc, fooFileName, fooFileContent, fooRegularUser1, "USER");

        eventService.deleteById(Long.valueOf(eventId));

        mvc.perform(get(EVENT_ENDPOINT + eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminRequestsDeletedEvent_thenEventIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Integer eventId = uploadTestFile(mockMvc, fooFileName, fooFileContent, fooRegularUser1, "USER");

        eventService.deleteById(Long.valueOf(eventId));

        mvc.perform(get(EVENT_ENDPOINT + eventId))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(eventId),
                        jsonPath("$.status").value(Status.DELETED.name())
                );
    }

    @Test
    @Transactional
    @WithMockUser(username = fooRegularUser1, roles = {"USER"})
    void whenRegularUserRequestsAllEvents_thenListOfOnlyActiveEventsOfThatUserIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        String testFileName1 = generateFileName(fooFileName);
        String testFileName2 = generateFileName(fooFileName);

        Integer eventId1 = uploadTestFile(mockMvc, testFileName1, fooFileContent, fooRegularUser1, "USER");
        Integer eventId2 = uploadTestFile(mockMvc, testFileName2, fooFileContent, fooRegularUser1, "USER");

        eventService.deleteById(Long.valueOf(eventId2));

        mvc.perform(get(EVENT_ENDPOINT))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$.[0].id").value(eventId1)
                );
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminRequestsAllEvents_thenListOfAllEventsOfAllUsersIsReturned() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        String testFileName1 = generateFileName(fooFileName);
        String testFileName2 = generateFileName(fooFileName);

        uploadTestFile(mockMvc, testFileName1, fooFileContent, fooRegularUser1, "USER");
        uploadTestFile(mockMvc, testFileName2, fooFileContent, fooRegularUser2, "USER");

        mvc.perform(get(EVENT_ENDPOINT))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$", hasSize(2))
                );
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"USER"})
    void whenRegularUserTriesToDeleteAnEvent_thenNoAccessIsReturned() throws Exception {
        mvc.perform(delete(EVENT_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminDeletesAnEvent_thenEventStatusIsChangedToDeletedAndTimestampUpdated() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        Integer eventId = uploadTestFile(mockMvc, fooFileName, fooFileContent, fooRegularUser1, "USER");

        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        Event event = eventService.getById(Long.valueOf(eventId));
        Date initUpdatedTimestamp = event.getUpdated();

        mvc.perform(delete(EVENT_ENDPOINT + eventId))
                .andExpect(status().isNoContent());

        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        Event deletedEvent = eventService.getById(Long.valueOf(eventId));

        assertTrue(deletedEvent.getUpdated().after(initUpdatedTimestamp));
        assertEquals(Status.DELETED, deletedEvent.getStatus());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"USER"})
    void whenRegularUserTriesToUpdateAnEvent_thenNoAccessIsReturned() throws Exception {
        String requestBody = "fakeBody";

        mvc.perform(put(EVENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void whenModeratorOrAdminUpdatesAnEvent_thenStatusAndTimestampAreUpdated() throws Exception {
        when(amazonS3Service.upload(anyString(), anyString(), any(InputStream.class), anyMap()))
                .thenReturn("Uploaded");
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Integer eventId = uploadTestFile(mockMvc, fooFileName, fooFileContent, fooRegularUser1, "USER");

        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        Event event = eventService.getById(Long.valueOf(eventId));
        Date initUpdatedTimestamp = event.getUpdated();

        String requestBody = String.format("{\"id\": %d, \"eventType\": \"UPLOADED\",\"status\": \"%s\"}",
                eventId, Status.DELETED);

        mvc.perform(put(EVENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.status").value(Status.DELETED.name())
                );

        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        Event updatedEvent = eventService.getById(Long.valueOf(eventId));

        assertTrue(updatedEvent.getUpdated().after(initUpdatedTimestamp));
    }

    private Integer uploadTestFile(MockMvc mockMvc, String fileName, String fileContent,
                                   String user, String role) throws Exception {
        MockMultipartFile testFile = getMockMultipartFile(fileName, fileContent);
        MvcResult result = mockMvc.perform(multipart(FILE_ENDPOINT + "upload").file(testFile)
                        .with(user(user).roles(role)))
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.events.[0].id");
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
