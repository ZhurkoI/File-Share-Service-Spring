package org.zhurko.fileshareservicespring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhurko.fileshareservicespring.model.entity.UserRole;
import org.zhurko.fileshareservicespring.model.Status;
import org.zhurko.fileshareservicespring.model.entity.User;
import org.zhurko.fileshareservicespring.repository.RoleRepository;
import org.zhurko.fileshareservicespring.repository.UserRepository;
import org.zhurko.fileshareservicespring.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Service
public class UserServiceImpl implements UserService {


    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // TODO: получается, что каждый метод должен вести себя специфически, в зависимости от роли вызывающего пользователя:
    //  - Админ -- видеть/редактировать всех пользователе, включая DELETED
    //  - MODERATOR -- только видеть пользователей, включая DELETED
    //  - USER -- не имеет доступа к эндпоинту users вовсе
    @Override
    public User getById(Long id) {
        // TODO: перепимать логику в зависимости от роли пользователя, к-й запрашивает юзера
        User user = userRepository.findById(id).orElseThrow();
        if (user.getStatus() == Status.DELETED) {
            throw new NoSuchElementException();
        }

        return user;
    }

    @Override
    public User save(User user) {
        // TODO - здесь нельзя слепо сетить статус и роль.  Заменить двумя методами: register + get?

//        UserRole role = new UserRole();
        // TODO: роли нужно запрашивать из БД, как готовый объект

        UserRole roleUser = roleRepository.findByName("ROLE_USER");
        List<UserRole> roles = new ArrayList<>();
        roles.add(roleUser);

        user.setRoles(roles);
        user.setStatus(Status.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return (List<User>) userRepository.findAll();
    }
}
