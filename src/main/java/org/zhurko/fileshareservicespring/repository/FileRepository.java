package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zhurko.fileshareservicespring.entity.File;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    File findByPathAndFileName(String path, String name);

    @Query(value = "SELECT f.id, f.path, f.name, f.status, f.created, f.updated " +
            "FROM files f JOIN events e ON f.id = e.file_id " +
            "WHERE e.user_id = ?1", nativeQuery = true)
    List<File> findAllFilesOfSpecifiedUser(Long userId);

    @Query(value = "SELECT f.id, f.path, f.name, f.status, f.created, f.updated " +
            "FROM files f " +
            "JOIN events e ON f.id = e.file_id " +
            "JOIN users u ON e.user_id = u.id " +
            "WHERE u.id = ?1 AND f.id = ?2", nativeQuery = true)
    File findFileOfSpecifiedUser(Long userId, Long fileId);
}
