package com.cmips.controller;

import com.cmips.entity.Task;
import com.cmips.service.TaskService;
import com.cmips.service.WorkQueueSubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;
    private final WorkQueueSubscriptionService subscriptionService;

    public TaskController(TaskService taskService, WorkQueueSubscriptionService subscriptionService) {
        this.taskService = taskService;
        this.subscriptionService = subscriptionService;
    }
    
    @GetMapping
    public ResponseEntity<List<Task>> getUserTasks(
            @RequestParam String username,
            @RequestParam(required = false) Boolean includeSubscribedQueues) {
        List<Task> tasks = taskService.getUserTasks(username);
        
        // If includeSubscribedQueues is true, also fetch tasks from subscribed queues
        if (Boolean.TRUE.equals(includeSubscribedQueues)) {
            List<String> subscribedQueues = subscriptionService.getUserQueues(username);
            List<Task> queueTasks = taskService.getTasksFromSubscribedQueues(username, subscribedQueues);
            
            // Combine both lists, avoiding duplicates
            for (Task queueTask : queueTasks) {
                if (!tasks.stream().anyMatch(t -> t.getId().equals(queueTask.getId()))) {
                    tasks.add(queueTask);
                }
            }
        }
        
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{username}/{status}")
    public ResponseEntity<List<Task>> getUserTasksByStatus(
            @PathVariable String username,
            @PathVariable String status) {
        Task.TaskStatus taskStatus = taskService.getTaskStatusByName(status);
        List<Task> tasks = taskService.getUserTasksByStatus(username, taskStatus);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/count/{username}")
    public ResponseEntity<Map<String, Long>> getTaskCounts(@PathVariable String username) {
        Long openCount = taskService.getUserTaskCount(username);
        Long inProgressCount = taskService.getUserTaskCountByStatus(username, Task.TaskStatus.IN_PROGRESS);
        Long closedCount = taskService.getUserTaskCountByStatus(username, Task.TaskStatus.CLOSED);
        
        return ResponseEntity.ok(Map.of(
            "open", openCount,
            "inProgress", inProgressCount,
            "closed", closedCount
        ));
    }
    
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = taskService.createTask(task);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
        Task updated = taskService.updateTaskStatus(id, taskStatus);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task) {
        task.setId(id);
        Task updated = taskService.updateTask(task);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    
    // Work Queue endpoints
    @GetMapping("/queue/{queueName}")
    public ResponseEntity<List<Task>> getQueueTasks(@PathVariable String queueName) {
        List<Task> tasks = taskService.getQueueTasks(queueName);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/queue/{queueName}/role/{role}")
    public ResponseEntity<List<Task>> getQueueTasksByRole(
            @PathVariable String queueName,
            @PathVariable String role) {
        List<Task> tasks = taskService.getQueueTasksByRole(queueName, role);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/queues")
    public ResponseEntity<Map<String, Long>> getAvailableQueues() {
        Map<String, Long> queues = taskService.getAvailableQueues();
        return ResponseEntity.ok(queues);
    }
}




