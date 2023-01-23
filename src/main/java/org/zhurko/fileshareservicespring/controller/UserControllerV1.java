package org.zhurko.fileshareservicespring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhurko.fileshareservicespring.model.dto.UserDTO;
import org.zhurko.fileshareservicespring.model.entity.User;
import org.zhurko.fileshareservicespring.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserControllerV1 {
    @Autowired
    private UserService userService;

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long userId) {
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user;
        try {
            user = this.userService.getById(userId);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(UserDTO.fromEntity(user), HttpStatus.OK);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = this.userService.findAll();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<UserDTO> usersDTO = users.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        return new ResponseEntity<>(usersDTO, HttpStatus.OK);
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> saveUser (@RequestBody @Valid UserDTO userDTO) {
        if (userDTO == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User savedUser = this.userService.save(userDTO.toEntity());

        return new ResponseEntity<>(UserDTO.fromEntity(savedUser), HttpStatus.CREATED);
    }

    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateUser (@RequestBody @Valid UserDTO userDTO) {
        if (userDTO == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // TODO: нельзя слепо апдейтить save(DTO). Некоторые поля не доступны публично.
        User updatedUser = this.userService.save(userDTO.toEntity());

        return new ResponseEntity<>(UserDTO.fromEntity(updatedUser), HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> deleteUser (@PathVariable("id") Long userId) {
        // todo: вместо id передать строку, то из аннотации @PathVariable летит исключение и контроллер сам
        //  возвращает 400-Вad request. В логе ворнинг. Это допустимо? Как исправить?

        // TODO: когда передается нету ID URL, то контроллер САМ возвращает нерелевантный код - 405-method not allowed.
        //  Ворнинг в логе - Resolved [org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'DELETE' not supported].
        //  Такое нужно исправлять?
        User user;
        try {
            user = this.userService.getById(userId);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        this.userService.deleteById(userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

// TODO: 500-ка на попытке вставить дубликата пользователя (не перехваченный SQL эксепшен)
//  то же самое может быть при попытке проапдейтить пользователя на уже существующего в БД