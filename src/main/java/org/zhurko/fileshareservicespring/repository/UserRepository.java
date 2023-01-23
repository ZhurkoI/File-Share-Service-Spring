package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.repository.CrudRepository;
import org.zhurko.fileshareservicespring.model.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByUsername(String username);
}
