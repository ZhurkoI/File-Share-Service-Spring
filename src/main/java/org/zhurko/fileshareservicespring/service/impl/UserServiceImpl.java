package org.zhurko.fileshareservicespring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.zhurko.fileshareservicespring.entity.Status;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.entity.UserRole;
import org.zhurko.fileshareservicespring.repository.RoleRepository;
import org.zhurko.fileshareservicespring.repository.UserRepository;
import org.zhurko.fileshareservicespring.service.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(User user) {
        UserRole roleUser = roleRepository.findByName("ROLE_USER");
        List<UserRole> roles = new ArrayList<>();
        roles.add(roleUser);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);
        user.setStatus(Status.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        User result = userRepository.findById(user.getId()).orElseThrow();

        if (user.getStatus() == Status.ACTIVE & result.getStatus() == Status.DELETED) {
            result.setStatus(Status.ACTIVE);
        }

        if (user.getFirstName() != null) {
            result.setFirstName(user.getFirstName());
        }

        if (user.getLastName() != null) {
            result.setLastName(user.getLastName());
        }

        if (user.getRoles() != null) {
            List<UserRole> roles = user.getRoles().
                    stream()
                    .map(r -> roleRepository.findByName(r.getName()))
                    .collect(Collectors.toList());
            result.setRoles(roles);
        }

        if (user.getStatus() != null) {
            result.setStatus(user.getStatus());
        }

        return userRepository.save(result);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Override
    public User findByUsername(String username) {
        User result = userRepository.findByUsername(username);
        if (result == null) {
            throw new NoSuchElementException("User '" + username + "' not found");
        }

        return result;
    }

    @Override
    public void deleteById(Long id) {
        User result = userRepository.findById(id).orElseThrow();
        result.setStatus(Status.DELETED);
        result.setUpdated(new Date());
        userRepository.save(result);
    }
}
