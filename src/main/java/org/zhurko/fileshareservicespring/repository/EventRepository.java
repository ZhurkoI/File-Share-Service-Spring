package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zhurko.fileshareservicespring.entity.Event;

import java.util.List;


public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByUserId(Long userId);
}
