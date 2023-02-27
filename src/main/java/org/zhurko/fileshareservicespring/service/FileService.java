package org.zhurko.fileshareservicespring.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhurko.fileshareservicespring.entity.File;

import java.io.IOException;
import java.util.List;

public interface FileService {

    File upload(MultipartFile file) throws IOException;

    File update(File file);

    File getById(Long id);

    List<File> getAll();

    void deleteById(Long id);
}
