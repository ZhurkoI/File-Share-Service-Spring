package org.zhurko.fileshareservicespring.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhurko.fileshareservicespring.entity.Event;
import org.zhurko.fileshareservicespring.entity.EventType;
import org.zhurko.fileshareservicespring.entity.File;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.repository.FileRepository;
import org.zhurko.fileshareservicespring.repository.UserRepository;
import org.zhurko.fileshareservicespring.service.AmazonS3Service;
import org.zhurko.fileshareservicespring.service.EventService;
import org.zhurko.fileshareservicespring.service.FileService;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Service
public class FileServiceImpl implements FileService {

    private final AmazonS3Service amazonS3Service;
    private final EventService eventService;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileServiceImpl(AmazonS3Service amazonS3Service, EventService eventService,
                           FileRepository fileRepository, UserRepository userRepository) {
        this.amazonS3Service = amazonS3Service;
        this.eventService = eventService;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Override
    public File upload(MultipartFile file) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        User user = userRepository.findByUsername(getUsernameOfCurrentPrincipal());

        String remoteFilePath = String.format("%s/%s", user.getUsername(), file.getOriginalFilename());

        String putObjectResult = amazonS3Service.upload(bucketName, remoteFilePath, file.getInputStream(), metadata);
        if (putObjectResult.isEmpty()) {
            throw new RuntimeException("File wasn't saved to remote storage");
        }

        File savedFile = createFile(file.getOriginalFilename(), user.getUsername());
        Event uploadEvent = createEvent(savedFile, user);
        user.addEvent(uploadEvent);
        savedFile.addEvent(uploadEvent);

        eventService.save(uploadEvent);

        return savedFile;
    }

    private File createFile(String fileName, String username) {
        Date date = new Date();
        File fileToSave = new File();
        fileToSave.setPath(String.format("%s/%s", bucketName, username));
        fileToSave.setFileName(fileName);
        fileToSave.setStatus(Status.ACTIVE);
        fileToSave.setCreated(date);
        fileToSave.setUpdated(date);

        return fileRepository.save(fileToSave);
    }

    private Event createEvent(File file, User user) {
        Date date = new Date();
        Event event = new Event();
        event.setEventType(EventType.UPLOADED);
        event.setStatus(Status.ACTIVE);
        event.setFile(file);
        event.setUser(user);
        event.setCreated(date);
        event.setUpdated(date);

        return event;
    }

    @Override
    public File update(File file) {
        File result = fileRepository.findById(file.getId()).orElseThrow(NoSuchElementException::new);
        result.setStatus(file.getStatus());
        result.setUpdated(new Date());

        return fileRepository.save(result);
    }

    @Override
    public File getById(Long fileId) {
        if (isCurrentUserNotAdminOrModerator()) {
            File result = fileRepository.findFileOfSpecifiedUser(getUsernameOfCurrentPrincipal(), fileId);
            if (result == null) {
                throw new NoSuchElementException();
            }
            if (result.getStatus().equals(Status.ACTIVE)) {
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        return fileRepository.findById(fileId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<File> getAll() {
        if (isCurrentUserNotAdminOrModerator()) {
            List<File> result = fileRepository.findAllFilesOfSpecifiedUser(getUsernameOfCurrentPrincipal());

            return result.stream()
                    .filter(f -> (!f.getStatus().equals(Status.DELETED)))
                    .collect(Collectors.toList());
        }

        return fileRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        File file = fileRepository.findById(id).orElseThrow(NoSuchElementException::new);
        file.setStatus(Status.DELETED);
        file.setUpdated(new Date());
        fileRepository.save(file);
    }

    private boolean isCurrentUserNotAdminOrModerator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().noneMatch(a -> (a.getAuthority().equals("ROLE_ADMIN"))
                || (a.getAuthority().equals("ROLE_MODERATOR")))) {
            return true;
        }
        return false;
    }

    private String getUsernameOfCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
