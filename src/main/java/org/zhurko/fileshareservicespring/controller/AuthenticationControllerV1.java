package org.zhurko.fileshareservicespring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhurko.fileshareservicespring.dto.AuthenticationRequestDto;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.security.jwt.JwtTokenProvider;
import org.zhurko.fileshareservicespring.service.UserService;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(value = "api/v1/auth/")
public class AuthenticationControllerV1 {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Autowired
    public AuthenticationControllerV1(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("login")
    public ResponseEntity login(@RequestBody AuthenticationRequestDto requestDto) {
        System.out.println("========================");
        System.out.println(System.getProperty("aws.accessKeyId"));
        System.out.println(System.getProperty("aws.secretAccessKey"));
        System.out.println("------------");
        System.out.println("Foo-key envVar - " + System.getenv("AWS_ACCESS_KEY_ID"));
        System.out.println("Foo-secret envVar - " + System.getenv("AWS_SECRET_ACCESS_KEY"));
        System.out.println("========================");
        try {
            String username = requestDto.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User with '" + username + "' not found");
            }

            String token = jwtTokenProvider.createToken(username, user.getRoles());

            Map<Object, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}