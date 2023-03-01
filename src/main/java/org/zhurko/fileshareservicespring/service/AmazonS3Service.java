package org.zhurko.fileshareservicespring.service;

import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;


public interface AmazonS3Service {

    String upload(String bucketName, String objectKey, InputStream inputStream, Map<String, String> metadata)
            throws FileAlreadyExistsException;
}
