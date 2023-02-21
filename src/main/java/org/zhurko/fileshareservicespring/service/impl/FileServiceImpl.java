package org.zhurko.fileshareservicespring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.zhurko.fileshareservicespring.repository.EventRepository;
import org.zhurko.fileshareservicespring.repository.FileRepository;
import org.zhurko.fileshareservicespring.repository.UserRepository;
import org.zhurko.fileshareservicespring.security.jwt.JwtUser;
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

    @Autowired
    private AmazonS3Service amazonS3Service;

    @Autowired
    private EventService eventService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Override
    public File upload(MultipartFile file) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        User user = userRepository.findByUsername(getCurrentJwtUser().getUsername());

        String remoteFilePath = String.format("%s/%s", user.getUsername(), file.getOriginalFilename());

        String putObjectResult = amazonS3Service.upload(bucketName, remoteFilePath, file.getInputStream(), metadata);
        if (putObjectResult.isEmpty()) {
            throw new RuntimeException("File wasn't saved to remote storage");
        }

        Date date = new Date();

        File fileToSave = new File();
        fileToSave.setPath(String.format("%s/%s", bucketName, user.getUsername()));
        fileToSave.setFileName(file.getOriginalFilename());
        fileToSave.setStatus(Status.ACTIVE);
        fileToSave.setCreated(date);
        fileToSave.setUpdated(date);

        File savedFile = fileRepository.save(fileToSave);

        Event uploadEvent = new Event();
        uploadEvent.setEventType(EventType.UPLOADED);
        uploadEvent.setStatus(Status.ACTIVE);
        uploadEvent.setCreated(date);
        uploadEvent.setUpdated(date);

        user.addEvent(uploadEvent);
        savedFile.addEvent(uploadEvent);

        eventService.save(uploadEvent);

        return savedFile;
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
        JwtUser currentJwtUser = getCurrentJwtUser();
        if (isNotAdminOrModerator(currentJwtUser)) {
            File result = fileRepository.findById(fileId).orElseThrow(NoSuchElementException::new);
            // TODO: bug - returns the file of another user.
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
        JwtUser currentJwtUser = getCurrentJwtUser();
        if (isNotAdminOrModerator(currentJwtUser)) {
            List<File> result = fileRepository.findFilesOfSpecifiedUser(currentJwtUser.getId());

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

    private JwtUser getCurrentJwtUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (JwtUser) auth.getPrincipal();
    }

    private boolean isNotAdminOrModerator(JwtUser user) {
        if (user.getAuthorities().stream().noneMatch(a -> (a.getAuthority().equals("ROLE_ADMIN"))
                || (a.getAuthority().equals("ROLE_MODERATOR")))) {
            return true;
        }
        return false;
    }
}
