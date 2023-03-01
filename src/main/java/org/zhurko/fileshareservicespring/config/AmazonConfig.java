package org.zhurko.fileshareservicespring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
public class AmazonConfig {

    @Value("${aws.s3.region}")
    private String s3Region;

    @Bean
    public S3Client s3Client() {
        Region region = Region.of(s3Region);
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        return S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
