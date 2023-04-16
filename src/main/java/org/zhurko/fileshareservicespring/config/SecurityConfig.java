package org.zhurko.fileshareservicespring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.zhurko.fileshareservicespring.security.jwt.JwtConfigurer;
import org.zhurko.fileshareservicespring.security.jwt.JwtTokenProvider;


@Configuration
public class SecurityConfig {

    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests(auth -> {
                    auth.antMatchers(LOGIN_ENDPOINT).permitAll();
                    auth.antMatchers(HttpMethod.GET, "/api/v1/users/*").hasAnyRole("ADMIN", "MODERATOR");
                    auth.antMatchers(HttpMethod.POST, "/api/v1/users/").hasRole("ADMIN");
                    auth.antMatchers(HttpMethod.PUT, "/api/v1/users/").hasRole("ADMIN");
                    auth.antMatchers(HttpMethod.DELETE, "/api/v1/users/*").hasRole("ADMIN");
                    auth.antMatchers(HttpMethod.POST, "/api/v1/files/upload").hasAnyRole("ADMIN", "MODERATOR", "USER");
                    auth.antMatchers(HttpMethod.GET, "/api/v1/files/*").hasAnyRole("ADMIN", "MODERATOR", "USER");
                    auth.antMatchers(HttpMethod.PUT, "/api/v1/files/").hasAnyRole("ADMIN", "MODERATOR");
                    auth.antMatchers(HttpMethod.DELETE, "/api/v1/files/*").hasAnyRole("ADMIN", "MODERATOR");
                    auth.antMatchers(HttpMethod.GET, "/api/v1/events/*").hasAnyRole("ADMIN", "MODERATOR", "USER");
                    auth.antMatchers(HttpMethod.PUT, "/api/v1/events/").hasAnyRole("ADMIN", "MODERATOR");
                    auth.antMatchers(HttpMethod.DELETE, "/api/v1/events/*").hasAnyRole("ADMIN", "MODERATOR");
                    auth.anyRequest().denyAll();
                })
                .apply(new JwtConfigurer(jwtTokenProvider));

        return http.build();
    }
}
