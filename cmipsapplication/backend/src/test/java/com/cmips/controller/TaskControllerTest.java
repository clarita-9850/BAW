package com.cmips.controller;

import com.cmips.entity.Task;
import com.cmips.service.TaskService;
import com.cmips.service.WorkQueueSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskController
 * 
 * Tests cover:
 * - CRUD operations
 * - Task status management
 * - Work queue operations
 * - User task retrieval
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskController Tests")
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private WorkQueueSubscriptionService subscriptionService;

    @InjectMocks
    private TaskController taskController;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_QUEUE = "address-changes";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should get user tasks successfully")
    void testGetUserTasks_Success() {
        // Arrange
        List<Task> mockTasks = createMockTasks(2);
        when(taskService.getUserTasks(TEST_USERNAME)).thenReturn(mockTasks);

        // Act
        ResponseEntity<List<Task>> response = taskController.getUserTasks(TEST_USERNAME, false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(taskService, times(1)).getUserTasks(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should get user tasks with subscribed queues")
    void testGetUserTasks_WithSubscribedQueues() {
        // Arrange
        List<Task> userTasks = createMockTasks(2);
        List<Task> queueTasks = createMockTasks(1);
        List<String> subscribedQueues = Arrays.asList(TEST_QUEUE);

        when(taskService.getUserTasks(TEST_USERNAME)).thenReturn(userTasks);
        when(subscriptionService.getUserQueues(TEST_USERNAME)).thenReturn(subscribedQueues);
        when(taskService.getTasksFromSubscribedQueues(TEST_USERNAME, subscribedQueues))
                .thenReturn(queueTasks);

        // Act
        ResponseEntity<List<Task>> response = taskController.getUserTasks(TEST_USERNAME, true);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(taskService, times(1)).getUserTasks(TEST_USERNAME);
        verify(subscriptionService, times(1)).getUserQueues(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void testGetTaskById_Success() {
        // Arrange
        Long taskId = 1L;
        Task mockTask = createMockTask(taskId);
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(mockTask));

        // Act
        ResponseEntity<Task> response = taskController.getTaskById(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taskId, response.getBody().getId());
        verify(taskService, times(1)).getTaskById(taskId);
    }

    @Test
    @DisplayName("Should return NotFound when task does not exist")
    void testGetTaskById_NotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Task> response = taskController.getTaskById(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(taskService, times(1)).getTaskById(taskId);
    }

    @Test
    @DisplayName("Should get tasks by status successfully")
    void testGetUserTasksByStatus_Success() {
        // Arrange
        String status = "OPEN";
        List<Task> mockTasks = createMockTasks(2);
        when(taskService.getTaskStatusByName(status)).thenReturn(Task.TaskStatus.OPEN);
        when(taskService.getUserTasksByStatus(TEST_USERNAME, Task.TaskStatus.OPEN))
                .thenReturn(mockTasks);

        // Act
        ResponseEntity<List<Task>> response = taskController.getUserTasksByStatus(TEST_USERNAME, status);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(taskService, times(1)).getUserTasksByStatus(TEST_USERNAME, Task.TaskStatus.OPEN);
    }

    @Test
    @DisplayName("Should get task counts successfully")
    void testGetTaskCounts_Success() {
        // Arrange
        Long openCount = 5L;
        Long inProgressCount = 3L;
        Long closedCount = 2L;

        when(taskService.getUserTaskCount(TEST_USERNAME)).thenReturn(openCount);
        when(taskService.getUserTaskCountByStatus(TEST_USERNAME, Task.TaskStatus.IN_PROGRESS))
                .thenReturn(inProgressCount);
        when(taskService.getUserTaskCountByStatus(TEST_USERNAME, Task.TaskStatus.CLOSED))
                .thenReturn(closedCount);

        // Act
        ResponseEntity<Map<String, Long>> response = taskController.getTaskCounts(TEST_USERNAME);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(openCount, response.getBody().get("open"));
        assertEquals(inProgressCount, response.getBody().get("inProgress"));
        assertEquals(closedCount, response.getBody().get("closed"));
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask_Success() {
        // Arrange
        Task taskToCreate = createMockTask(null);
        Task createdTask = createMockTask(1L);
        when(taskService.createTask(any(Task.class))).thenReturn(createdTask);

        // Act
        ResponseEntity<Task> response = taskController.createTask(taskToCreate);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("Should update task successfully")
    void testUpdateTask_Success() {
        // Arrange
        Long taskId = 1L;
        Task taskToUpdate = createMockTask(taskId);
        Task updatedTask = createMockTask(taskId);
        updatedTask.setTitle("Updated Title");
        when(taskService.updateTask(any(Task.class))).thenReturn(updatedTask);

        // Act
        ResponseEntity<Task> response = taskController.updateTask(taskId, taskToUpdate);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taskId, response.getBody().getId());
        verify(taskService, times(1)).updateTask(any(Task.class));
    }

    @Test
    @DisplayName("Should update task status successfully")
    void testUpdateTaskStatus_Success() {
        // Arrange
        Long taskId = 1L;
        Map<String, String> request = Map.of("status", "IN_PROGRESS");
        Task updatedTask = createMockTask(taskId);
        updatedTask.setStatus(Task.TaskStatus.IN_PROGRESS);
        when(taskService.updateTaskStatus(taskId, Task.TaskStatus.IN_PROGRESS))
                .thenReturn(updatedTask);

        // Act
        ResponseEntity<Task> response = taskController.updateTaskStatus(taskId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Task.TaskStatus.IN_PROGRESS, response.getBody().getStatus());
        verify(taskService, times(1)).updateTaskStatus(taskId, Task.TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask_Success() {
        // Arrange
        Long taskId = 1L;
        doNothing().when(taskService).deleteTask(taskId);

        // Act
        ResponseEntity<Void> response = taskController.deleteTask(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    @DisplayName("Should get queue tasks successfully")
    void testGetQueueTasks_Success() {
        // Arrange
        List<Task> mockTasks = createMockTasks(3);
        when(taskService.getQueueTasks(TEST_QUEUE)).thenReturn(mockTasks);

        // Act
        ResponseEntity<List<Task>> response = taskController.getQueueTasks(TEST_QUEUE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(taskService, times(1)).getQueueTasks(TEST_QUEUE);
    }

    @Test
    @DisplayName("Should get queue tasks by role successfully")
    void testGetQueueTasksByRole_Success() {
        // Arrange
        String role = "CASE_WORKER";
        List<Task> mockTasks = createMockTasks(2);
        when(taskService.getQueueTasksByRole(TEST_QUEUE, role)).thenReturn(mockTasks);

        // Act
        ResponseEntity<List<Task>> response = taskController.getQueueTasksByRole(TEST_QUEUE, role);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(taskService, times(1)).getQueueTasksByRole(TEST_QUEUE, role);
    }

    @Test
    @DisplayName("Should get available queues successfully")
    void testGetAvailableQueues_Success() {
        // Arrange
        Map<String, Long> queueCounts = Map.of(
                "address-changes", 5L,
                "status-updates", 3L
        );
        when(taskService.getAvailableQueues()).thenReturn(queueCounts);

        // Act
        ResponseEntity<Map<String, Long>> response = taskController.getAvailableQueues();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(taskService, times(1)).getAvailableQueues();
    }

    // Helper methods
    private Task createMockTask(Long id) {
        Task task = new Task();
        if (id != null) {
            task.setId(id);
        }
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setAssignedTo(TEST_USERNAME);
        task.setStatus(Task.TaskStatus.OPEN);
        task.setPriority(Task.TaskPriority.MEDIUM);
        task.setWorkQueue(TEST_QUEUE);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private List<Task> createMockTasks(int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            tasks.add(createMockTask((long) i));
        }
        return tasks;
    }
}







