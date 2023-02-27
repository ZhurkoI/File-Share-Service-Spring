package org.zhurko.fileshareservicespring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhurko.fileshareservicespring.service.AmazonS3Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;

@Service
public class AmazonS3ServiceImpl implements AmazonS3Service {

    @Autowired
    private S3Client s3Client;

    @Override
    public String upload(String bucketName, String objectKey, InputStream inputStream, Map<String, String> metadata)
            throws FileAlreadyExistsException {
        boolean ifExist = false;
        ifExist = doesFileExist(bucketName, objectKey);

        if (ifExist) {
            throw new FileAlreadyExistsException("File " + objectKey + " already exists for the user");
        }

        try {
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .metadata(metadata)
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    putOb,
                    RequestBody.fromInputStream(inputStream,
                    inputStream.available()));
            return response.eTag();
        } catch (IOException | S3Exception e) {
            System.err.println(e.getMessage());
            // todo: залогировать вместо этого
        }

        return "";
    }

    private boolean doesFileExist(String bucketName, String keyName) {
        try {
            HeadObjectRequest objectRequest = HeadObjectRequest.builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            s3Client.headObject(objectRequest);

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new RuntimeException("Cannot check existence of the file");
        }

        return true;
    }
}
