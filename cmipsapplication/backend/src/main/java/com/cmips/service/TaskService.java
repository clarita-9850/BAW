package com.cmips.service;

import com.cmips.entity.Task;
import com.cmips.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(Task task) {
        log.info("Creating task: {}", task.getTitle());
        return taskRepository.save(task);
    }

    public List<Task> getUserTasks(String username) {
        return taskRepository.findByAssignedTo(username);
    }

    public List<Task> getUserTasksByStatus(String username, Task.TaskStatus status) {
        return taskRepository.findByAssignedToAndStatus(username, status);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task updateTaskStatus(Long id, Task.TaskStatus newStatus) {
        return taskRepository.findById(id)
            .map(task -> {
                task.setStatus(newStatus);
                task.setUpdatedAt(LocalDateTime.now());
                return taskRepository.save(task);
            })
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    public Task updateTask(Task task) {
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Long getUserTaskCount(String username) {
        return taskRepository.countByAssignedToAndStatus(username, Task.TaskStatus.OPEN);
    }

    public Long getUserTaskCountByStatus(String username, Task.TaskStatus status) {
        return taskRepository.countByAssignedToAndStatus(username, status);
    }

    public Task.TaskStatus getTaskStatusByName(String status) {
        return Task.TaskStatus.valueOf(status.toUpperCase());
    }

    // Work Queue methods
    public List<Task> getQueueTasks(String queueName) {
        log.info("Fetching tasks for queue: {}", queueName);
        return taskRepository.findByWorkQueue(queueName);
    }

    public List<Task> getQueueTasksByRole(String queueName, String role) {
        log.info("Fetching tasks for queue: {} with role: {}", queueName, role);
        return taskRepository.findByQueueRole(role);
    }

    public java.util.Map<String, Long> getAvailableQueues() {
        log.info("Fetching available queues");
        java.util.Map<String, Long> queueCounts = new java.util.HashMap<>();

        // Get all distinct queue names and their task counts
        List<Task> allTasks = taskRepository.findAll();
        for (Task task : allTasks) {
            if (task.getWorkQueue() != null) {
                queueCounts.put(task.getWorkQueue(),
                    queueCounts.getOrDefault(task.getWorkQueue(), 0L) + 1);
            }
        }

        return queueCounts;
    }

    /**
     * Get tasks from queues that a user is subscribed to
     */
    public List<Task> getTasksFromSubscribedQueues(String username, List<String> subscribedQueues) {
        log.info("Fetching tasks for user {} from subscribed queues: {}", username, subscribedQueues);
        if (subscribedQueues == null || subscribedQueues.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // Get all tasks from the subscribed queues
        List<Task> tasks = new java.util.ArrayList<>();
        for (String queueName : subscribedQueues) {
            List<Task> queueTasks = taskRepository.findByWorkQueue(queueName);
            tasks.addAll(queueTasks);
        }

        return tasks;
    }
}
