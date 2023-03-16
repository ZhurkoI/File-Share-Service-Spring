package org.zhurko.fileshareservicespring;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.zhurko.fileshareservicespring.security.jwt.JwtUser;


@Profile("FileService-Test")
@Configuration
public class FileServiceTestConfiguration {
//
//    @Bean
//    @Primary
//    public JwtUser jwtUser() {
//        return Mockito.mock(JwtUser.class);
//    }
}
