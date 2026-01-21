package com.rh360.rh360.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    List<Task> findByParentTaskId(UUID parentTaskId);
    
    List<Task> findByResponsibleUserId(UUID userId);
    
    List<Task> findByTeamId(UUID teamId);
    
    List<Task> findByParentTaskIsNull();
}
