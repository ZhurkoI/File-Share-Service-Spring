package org.zhurko.fileshareservicespring.repository;

import org.springframework.data.repository.CrudRepository;
import org.zhurko.fileshareservicespring.model.entity.File;

public interface FileRepository extends CrudRepository<File, Long> {
}
