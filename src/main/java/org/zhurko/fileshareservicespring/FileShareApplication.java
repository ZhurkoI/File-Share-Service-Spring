package org.zhurko.fileshareservicespring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class FileShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileShareApplication.class, args);
    }
}
