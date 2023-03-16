package org.zhurko.fileshareservicespring.service;

import org.zhurko.fileshareservicespring.entity.User;

import java.util.List;


public interface UserService {

    User register(User user);

    User update(User user);

    List<User> getAll();

    User getById(Long id);

    User getByUsername(String username);

    void deleteById(Long id);
}
