package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zhurko.fileshareservicespring.entity.Event;

import java.util.List;


public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(value = "SELECT e.id, e.type, e.status, e.created, e.updated, e.file_id, e.user_id " +
            "FROM events e " +
            "WHERE e.user_id = " +
            "(" +
                "SELECT u.id " +
                "FROM users u " +
                "WHERE u.username = ?1 " +
            ")", nativeQuery = true)
    List<Event> findAllByUsername(String username);
}
