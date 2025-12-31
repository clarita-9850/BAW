package com.cmips.service;

import com.cmips.entity.Task;
import com.cmips.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_QUEUE = "address-changes";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask_Success() {
        // Arrange
        Task task = createMockTask(1L);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.createTask(task);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    @DisplayName("Should get user tasks successfully")
    void testGetUserTasks_Success() {
        // Arrange
        List<Task> mockTasks = Arrays.asList(createMockTask(1L), createMockTask(2L));
        when(taskRepository.findByAssignedTo(TEST_USERNAME)).thenReturn(mockTasks);

        // Act
        List<Task> result = taskService.getUserTasks(TEST_USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findByAssignedTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should get user tasks by status successfully")
    void testGetUserTasksByStatus_Success() {
        // Arrange
        Task.TaskStatus status = Task.TaskStatus.OPEN;
        List<Task> mockTasks = Arrays.asList(createMockTask(1L));
        when(taskRepository.findByAssignedToAndStatus(TEST_USERNAME, status)).thenReturn(mockTasks);

        // Act
        List<Task> result = taskService.getUserTasksByStatus(TEST_USERNAME, status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByAssignedToAndStatus(TEST_USERNAME, status);
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void testGetTaskById_Success() {
        // Arrange
        Long taskId = 1L;
        Task mockTask = createMockTask(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));

        // Act
        Optional<Task> result = taskService.getTaskById(taskId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(taskId, result.get().getId());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Should return empty when task not found")
    void testGetTaskById_NotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskService.getTaskById(taskId);

        // Assert
        assertFalse(result.isPresent());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Should update task status successfully")
    void testUpdateTaskStatus_Success() {
        // Arrange
        Long taskId = 1L;
        Task.TaskStatus newStatus = Task.TaskStatus.IN_PROGRESS;
        Task existingTask = createMockTask(taskId);
        existingTask.setStatus(Task.TaskStatus.OPEN);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.updateTaskStatus(taskId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when task not found for status update")
    void testUpdateTaskStatus_NotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> taskService.updateTaskStatus(taskId, Task.TaskStatus.CLOSED));
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should update task successfully")
    void testUpdateTask_Success() {
        // Arrange
        Task task = createMockTask(1L);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.updateTask(task);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask_Success() {
        // Arrange
        Long taskId = 1L;
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    @DisplayName("Should get user task count successfully")
    void testGetUserTaskCount_Success() {
        // Arrange
        Long expectedCount = 5L;
        when(taskRepository.countByAssignedToAndStatus(TEST_USERNAME, Task.TaskStatus.OPEN))
                .thenReturn(expectedCount);

        // Act
        Long result = taskService.getUserTaskCount(TEST_USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCount, result);
        verify(taskRepository, times(1)).countByAssignedToAndStatus(TEST_USERNAME, Task.TaskStatus.OPEN);
    }

    @Test
    @DisplayName("Should get queue tasks successfully")
    void testGetQueueTasks_Success() {
        // Arrange
        List<Task> mockTasks = Arrays.asList(createMockTask(1L));
        when(taskRepository.findByWorkQueue(TEST_QUEUE)).thenReturn(mockTasks);

        // Act
        List<Task> result = taskService.getQueueTasks(TEST_QUEUE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByWorkQueue(TEST_QUEUE);
    }

    @Test
    @DisplayName("Should get tasks from subscribed queues successfully")
    void testGetTasksFromSubscribedQueues_Success() {
        // Arrange
        List<String> subscribedQueues = Arrays.asList("queue1", "queue2");
        List<Task> queue1Tasks = Arrays.asList(createMockTask(1L));
        List<Task> queue2Tasks = Arrays.asList(createMockTask(2L));
        
        when(taskRepository.findByWorkQueue("queue1")).thenReturn(queue1Tasks);
        when(taskRepository.findByWorkQueue("queue2")).thenReturn(queue2Tasks);

        // Act
        List<Task> result = taskService.getTasksFromSubscribedQueues(TEST_USERNAME, subscribedQueues);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no subscribed queues")
    void testGetTasksFromSubscribedQueues_Empty() {
        // Act
        List<Task> result = taskService.getTasksFromSubscribedQueues(TEST_USERNAME, Collections.emptyList());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Helper methods
    private Task createMockTask(Long id) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Test Task " + id);
        task.setDescription("Test Description");
        task.setStatus(Task.TaskStatus.OPEN);
        task.setPriority(Task.TaskPriority.MEDIUM);
        task.setAssignedTo(TEST_USERNAME);
        task.setWorkQueue(TEST_QUEUE);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }
}







