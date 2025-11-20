package com.cmips.repository;

import com.cmips.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(String assignedTo);
    List<Task> findByStatus(Task.TaskStatus status);
    List<Task> findByAssignedToAndStatus(String assignedTo, Task.TaskStatus status);
    Long countByAssignedToAndStatus(String assignedTo, Task.TaskStatus status);
    
    // Work Queue methods
    List<Task> findByWorkQueue(String workQueue);
    List<Task> findByWorkQueueAndStatus(String workQueue, Task.TaskStatus status);
    List<Task> findByQueueRole(String queueRole);
}

