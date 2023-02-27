package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zhurko.fileshareservicespring.entity.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserId(Long id);   // TODO: нужно?

    Event findByFileIdAndUserId(Long fileId, Long userId); // TODO: нужно?

    List<Event> findAllByUserId(Long userId);
}
