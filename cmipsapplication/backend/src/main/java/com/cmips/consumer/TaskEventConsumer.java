package com.cmips.consumer;

import com.cmips.entity.Notification;
import com.cmips.entity.Task;
import com.cmips.event.BaseEvent;
import com.cmips.service.NotificationService;
import com.cmips.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class TaskEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaskEventConsumer.class);

    private final TaskService taskService;
    private final NotificationService notificationService;

    public TaskEventConsumer(TaskService taskService, NotificationService notificationService) {
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "cmips-case-events", groupId = "task-consumer-group")
    public void handleCaseEvent(BaseEvent event) {
        log.info("Received case event: {}", event.getEventType());

        if ("case.created".equals(event.getEventType())) {
            createCaseCreatedTask(event);
        } else if ("case.address.changed".equals(event.getEventType())) {
            createAddressValidationTask(event);
        }
    }

    @KafkaListener(topics = "cmips-timesheet-events", groupId = "task-consumer-group")
    public void handleTimesheetEvent(BaseEvent event) {
        log.info("Received timesheet event: {}", event.getEventType());

        if ("timesheet.exception.detected".equals(event.getEventType())) {
            createTimesheetExceptionTask(event);
        } else if ("timesheet.overtime.violation".equals(event.getEventType())) {
            createOvertimeViolationTask(event);
        }
    }

    private void createCaseCreatedTask(BaseEvent event) {
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();

        Task task = Task.builder()
            .title("New Case Assignment - " + payload.get("recipient"))
            .description("A new case has been assigned to you")
            .assignedTo((String) payload.get("owner"))
            .status(Task.TaskStatus.OPEN)
            .priority(Task.TaskPriority.HIGH)
            .triggerCondition("Case created and assigned")
            .actionLink("/cases/" + payload.get("caseId"))
            .relatedEntityType("CASE")
            .dueDate(LocalDateTime.now().plusDays(5))
            .createdAt(LocalDateTime.now())
            .build();

        taskService.createTask(task);

        // Create notification
        Notification notification = Notification.builder()
            .userId((String) payload.get("owner"))
            .message("You have a new case assigned - " + payload.get("recipient"))
            .notificationType(Notification.NotificationType.INFO)
            .actionLink("/cases/" + payload.get("caseId"))
            .relatedEntityType("CASE")
            .readStatus(false)
            .createdAt(LocalDateTime.now())
            .build();

        notificationService.createNotification(notification);
    }

    private void createAddressValidationTask(BaseEvent event) {
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();

        String providerId = (String) payload.get("providerId");
        Boolean isOutsideCA = (Boolean) payload.get("isOutsideCA");

        // Only create task if address is outside California
        if (isOutsideCA == null || !isOutsideCA) {
            log.info("Address is in California, skipping task creation");
            return;
        }

        String description = providerId != null
            ? String.format("Provider %s has changed their address to outside California - requires review", providerId)
            : "Provider address changed to outside California - requires review";

        // Create task in PROVIDER_MANAGEMENT work queue
        Task task = Task.builder()
            .title("Address Validation Required - Provider: " + providerId)
            .description(description)
            .assignedTo("PROVIDER_MANAGEMENT") // Assign to queue, not specific user
            .status(Task.TaskStatus.OPEN)
            .priority(Task.TaskPriority.MEDIUM)
            .workQueue("PROVIDER_MANAGEMENT") // Set work queue
            .queueRole("CASE_WORKER") // Role required to access this queue
            .triggerCondition("Provider address changed outside CA")
            .actionLink("/cases/" + payload.get("caseId"))
            .relatedEntityType("CASE")
            .dueDate(LocalDateTime.now().plusDays(2))
            .createdAt(LocalDateTime.now())
            .build();

        taskService.createTask(task);
        log.info("Created address validation task in PROVIDER_MANAGEMENT queue for provider: {}", providerId);
    }

    private void createTimesheetExceptionTask(BaseEvent event) {
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();

        Task task = Task.builder()
            .title("Timesheet Exception - Provider: " + payload.get("providerId"))
            .description((String) payload.get("errorMessage"))
            .assignedTo("payroll_queue")
            .status(Task.TaskStatus.OPEN)
            .priority(Task.TaskPriority.valueOf((String) payload.get("priority")))
            .triggerCondition("Timesheet validation failed")
            .actionLink("/timesheets/" + payload.get("timesheetId"))
            .relatedEntityType("TIMESHEET")
            .relatedEntityId((Long) payload.get("timesheetId"))
            .dueDate(LocalDateTime.now().plusDays(1))
            .createdAt(LocalDateTime.now())
            .build();

        taskService.createTask(task);

        // Notify supervisor
        Notification notification = Notification.builder()
            .userId("supervisor")
            .message("Timesheet exception detected for provider: " + payload.get("providerId"))
            .notificationType(Notification.NotificationType.ALERT)
            .actionLink("/timesheets/" + payload.get("timesheetId"))
            .readStatus(false)
            .createdAt(LocalDateTime.now())
            .build();

        notificationService.createNotification(notification);
    }

    private void createOvertimeViolationTask(BaseEvent event) {
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();

        Task task = Task.builder()
            .title("Overtime Violation - Provider: " + payload.get("providerId"))
            .description("Provider submitted overtime hours exceeding limit")
            .assignedTo("supervisor")
            .status(Task.TaskStatus.OPEN)
            .priority(Task.TaskPriority.HIGH)
            .triggerCondition("Overtime violation")
            .actionLink("/timesheets/" + payload.get("timesheetId"))
            .relatedEntityType("TIMESHEET")
            .relatedEntityId((Long) payload.get("timesheetId"))
            .dueDate(LocalDateTime.now().plusDays(3))
            .createdAt(LocalDateTime.now())
            .build();

        taskService.createTask(task);
    }
}
