package org.zhurko.fileshareservicespring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
public class AmazonConfig {

//    @Value("${aws.accessKeyId}")
//    private String accessKey;
//
//    @Value("${aws.secretAccessKey}")
//    private String secretKey;

//    @Value("${aws.s3.region}")
//    private String testRegion;

    @Bean
    public S3Client s3Client() {
//        // TODO: регион вместе с кредлами предать в докер при старте докера (параметры запуска JVM)
        Region region = Region.EU_WEST_3;
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        return S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
