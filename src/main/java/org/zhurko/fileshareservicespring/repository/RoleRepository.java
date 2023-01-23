package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.repository.CrudRepository;
import org.zhurko.fileshareservicespring.model.entity.UserRole;

public interface RoleRepository extends CrudRepository<UserRole, Long> {

    UserRole findByName(String name);
}
