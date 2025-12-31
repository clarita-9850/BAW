package com.cmips.service;

import com.cmips.entity.ReportJobEntity;
import com.cmips.model.BIReportRequest;
import com.cmips.model.JobStatus;
import com.cmips.model.ReportResult;
import com.cmips.repository.ReportJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Unit tests for JobQueueService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JobQueueService Tests")
class JobQueueServiceTest {

    @Mock
    private ReportJobRepository jobRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JobQueueService jobQueueService;

    private static final String TEST_JOB_ID = "JOB_12345678";
    private static final String TEST_JWT = "test-jwt-token";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should queue report job successfully")
    void testQueueReportJob_Success() {
        // Arrange
        BIReportRequest request = createValidReportRequest();
        when(jobRepository.save(any(ReportJobEntity.class))).thenAnswer(invocation -> {
            ReportJobEntity job = invocation.getArgument(0);
            return job;
        });

        // Act
        String jobId = jobQueueService.queueReportJob(request, TEST_JWT);

        // Assert
        assertNotNull(jobId);
        assertTrue(jobId.startsWith("JOB_"));
        verify(jobRepository, times(1)).save(any(ReportJobEntity.class));
    }

    @Test
    @DisplayName("Should get job status successfully")
    void testGetJobStatus_Success() {
        // Arrange
        ReportJobEntity mockJob = createMockJobEntity(TEST_JOB_ID, "QUEUED");
        when(jobRepository.findByJobId(TEST_JOB_ID)).thenReturn(Optional.of(mockJob));

        // Act
        JobStatus status = jobQueueService.getJobStatus(TEST_JOB_ID);

        // Assert
        assertNotNull(status);
        assertEquals(TEST_JOB_ID, status.getJobId());
        assertEquals("QUEUED", status.getStatus());
    }

    @Test
    @DisplayName("Should get job result when completed")
    void testGetJobResult_Success() {
        // Arrange
        ReportJobEntity mockJob = createMockJobEntity(TEST_JOB_ID, "COMPLETED");
        mockJob.setResultPath("/tmp/report.csv");
        when(jobRepository.findByJobId(TEST_JOB_ID)).thenReturn(Optional.of(mockJob));

        // Act
        ReportResult result = jobQueueService.getJobResult(TEST_JOB_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_JOB_ID, result.getJobId());
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    @DisplayName("Should cancel job successfully")
    void testCancelJob_Success() {
        // Arrange
        ReportJobEntity mockJob = createMockJobEntity(TEST_JOB_ID, "QUEUED");
        when(jobRepository.findByJobId(TEST_JOB_ID)).thenReturn(Optional.of(mockJob));
        when(jobRepository.save(any(ReportJobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean result = jobQueueService.cancelJob(TEST_JOB_ID);

        // Assert
        assertTrue(result);
        assertEquals("CANCELLED", mockJob.getStatus());
        verify(jobRepository, times(1)).save(any(ReportJobEntity.class));
    }

    @Test
    @DisplayName("Should get jobs by status successfully")
    void testGetJobsByStatus_Success() {
        // Arrange
        List<ReportJobEntity> mockJobs = Arrays.asList(
                createMockJobEntity("JOB_001", "QUEUED"),
                createMockJobEntity("JOB_002", "QUEUED")
        );
        when(jobRepository.findByStatus("QUEUED")).thenReturn(mockJobs);

        // Act
        List<JobStatus> result = jobQueueService.getJobsByStatus("QUEUED");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jobRepository, times(1)).findByStatus("QUEUED");
    }

    @Test
    @DisplayName("Should get all jobs successfully")
    void testGetAllJobs_Success() {
        // Arrange
        List<ReportJobEntity> mockJobs = Arrays.asList(
                createMockJobEntity("JOB_001", "QUEUED"),
                createMockJobEntity("JOB_002", "COMPLETED")
        );
        when(jobRepository.findAll()).thenReturn(mockJobs);

        // Act
        List<JobStatus> result = jobQueueService.getAllJobs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update job status successfully")
    void testUpdateJobStatus_Success() {
        // Arrange
        ReportJobEntity mockJob = createMockJobEntity(TEST_JOB_ID, "QUEUED");
        when(jobRepository.findByJobId(TEST_JOB_ID)).thenReturn(Optional.of(mockJob));
        when(jobRepository.save(any(ReportJobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        jobQueueService.updateJobStatus(TEST_JOB_ID, "PROCESSING", null);

        // Assert
        assertEquals("PROCESSING", mockJob.getStatus());
        verify(jobRepository, times(1)).save(any(ReportJobEntity.class));
    }

    @Test
    @DisplayName("Should update job progress successfully")
    void testUpdateJobProgress_Success() {
        // Arrange
        ReportJobEntity mockJob = createMockJobEntity(TEST_JOB_ID, "PROCESSING");
        when(jobRepository.findByJobId(TEST_JOB_ID)).thenReturn(Optional.of(mockJob));
        when(jobRepository.save(any(ReportJobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        jobQueueService.updateJobProgress(TEST_JOB_ID, 50L, 100L);

        // Assert
        assertEquals(50L, mockJob.getProcessedRecords());
        verify(jobRepository, times(1)).save(any(ReportJobEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when job not found")
    void testGetJobStatus_NotFound() {
        // Arrange
        when(jobRepository.findByJobId(TEST_JOB_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jobQueueService.getJobStatus(TEST_JOB_ID));
    }

    // Helper methods
    private BIReportRequest createValidReportRequest() {
        BIReportRequest request = new BIReportRequest();
        request.setUserRole("ADMIN");
        request.setReportType("DAILY_REPORT");
        request.setTargetSystem("BUSINESS_OBJECTS");
        request.setDataFormat("CSV");
        request.setPriority(5);
        return request;
    }

    private ReportJobEntity createMockJobEntity(String jobId, String status) {
        ReportJobEntity job = new ReportJobEntity(jobId, "ADMIN", "DAILY_REPORT", "BUSINESS_OBJECTS");
        job.setStatus(status);
        job.setProgress(0);
        job.setCreatedAt(LocalDateTime.now());
        return job;
    }
}







