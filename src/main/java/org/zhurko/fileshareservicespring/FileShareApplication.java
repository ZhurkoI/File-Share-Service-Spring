package org.zhurko.fileshareservicespring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class FileShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileShareApplication.class, args);
    }
}


// TODO: подключить Liquibase/Flyway через стартер (как на Видео пo JWT)