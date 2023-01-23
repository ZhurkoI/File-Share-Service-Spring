package org.zhurko.fileshareservicespring.service;

import org.zhurko.fileshareservicespring.model.entity.User;

import java.util.List;

public interface UserService {

    User getById(Long id);

    User save(User user);

    void deleteById(Long id);

    List<User> findAll();
}
