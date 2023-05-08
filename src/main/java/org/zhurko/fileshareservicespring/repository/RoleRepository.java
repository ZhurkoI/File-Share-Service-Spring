package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zhurko.fileshareservicespring.entity.UserRole;


public interface RoleRepository extends JpaRepository<UserRole, Long> {

    UserRole findByName(String name);
}
