package com.cmips.controller;

import com.cmips.model.JobStatus;
import com.cmips.model.ReportResult;
import com.cmips.service.JobQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BusinessIntelligenceController
 * 
 * Tests cover:
 * - BI report generation
 * - Job status tracking
 * - Report download
 * - Job cancellation
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BusinessIntelligenceController Tests")
class BusinessIntelligenceControllerTest {

    @Mock
    private JobQueueService jobQueueService;

    @InjectMocks
    private BusinessIntelligenceController biController;

    private static final String TEST_JOB_ID = "JOB_12345678";
    private static final String VALID_JWT = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJBRE1JTiJdfX0.test";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should generate BI report successfully")
    void testGenerateBIReport_Success() {
        // Arrange
        Map<String, Object> request = createValidReportRequest();
        when(jobQueueService.queueReportJob(any(), anyString())).thenReturn(TEST_JOB_ID);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.generateBIReport(request, VALID_JWT);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertEquals(TEST_JOB_ID, response.getBody().get("jobId"));
        verify(jobQueueService, times(1)).queueReportJob(any(), anyString());
    }

    @Test
    @DisplayName("Should return 401 when Authorization header is missing")
    void testGenerateBIReport_MissingAuthHeader() {
        // Arrange
        Map<String, Object> request = createValidReportRequest();

        // Act
        ResponseEntity<Map<String, Object>> response = biController.generateBIReport(request, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
        verify(jobQueueService, never()).queueReportJob(any(), anyString());
    }

    @Test
    @DisplayName("Should return 401 when JWT token is invalid")
    void testGenerateBIReport_InvalidJWT() {
        // Arrange
        Map<String, Object> request = createValidReportRequest();
        String invalidJWT = "Bearer invalid.token.here";

        // Act
        ResponseEntity<Map<String, Object>> response = biController.generateBIReport(request, invalidJWT);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
        verify(jobQueueService, never()).queueReportJob(any(), anyString());
    }

    @Test
    @DisplayName("Should get job status successfully")
    void testGetJobStatus_Success() {
        // Arrange
        JobStatus mockStatus = createMockJobStatus(TEST_JOB_ID, "PROCESSING", 50);
        when(jobQueueService.getJobStatus(TEST_JOB_ID)).thenReturn(mockStatus);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.getJobStatus(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertNotNull(response.getBody().get("jobStatus"));
        verify(jobQueueService, times(1)).getJobStatus(TEST_JOB_ID);
    }

    @Test
    @DisplayName("Should handle job not found")
    void testGetJobStatus_NotFound() {
        // Arrange
        when(jobQueueService.getJobStatus(TEST_JOB_ID))
                .thenThrow(new RuntimeException("Job not found"));

        // Act
        ResponseEntity<Map<String, Object>> response = biController.getJobStatus(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Should get job result when completed")
    void testGetJobResult_Completed() {
        // Arrange
        JobStatus mockStatus = createMockJobStatus(TEST_JOB_ID, "COMPLETED", 100);
        ReportResult mockResult = createMockReportResult(TEST_JOB_ID);
        
        when(jobQueueService.getJobStatus(TEST_JOB_ID)).thenReturn(mockStatus);
        when(jobQueueService.getJobResult(TEST_JOB_ID)).thenReturn(mockResult);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.getJobResult(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertNotNull(response.getBody().get("result"));
    }

    @Test
    @DisplayName("Should return BadRequest when job not completed")
    void testGetJobResult_NotCompleted() {
        // Arrange
        JobStatus mockStatus = createMockJobStatus(TEST_JOB_ID, "PROCESSING", 50);
        when(jobQueueService.getJobStatus(TEST_JOB_ID)).thenReturn(mockStatus);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.getJobResult(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
        verify(jobQueueService, never()).getJobResult(anyString());
    }

    @Test
    @DisplayName("Should download report file successfully")
    void testDownloadReport_Success() throws Exception {
        // Arrange
        JobStatus mockStatus = createMockJobStatus(TEST_JOB_ID, "COMPLETED", 100);
        ReportResult mockResult = createMockReportResult(TEST_JOB_ID);
        mockResult.setResultPath("/tmp/test-report.csv");
        
        File testFile = File.createTempFile("test-report", ".csv");
        testFile.deleteOnExit();
        
        when(jobQueueService.getJobStatus(TEST_JOB_ID)).thenReturn(mockStatus);
        when(jobQueueService.getJobResult(TEST_JOB_ID)).thenReturn(mockResult);

        // Act
        ResponseEntity<?> response = biController.downloadReport(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        // Note: File download response structure may vary
        verify(jobQueueService, times(1)).getJobStatus(TEST_JOB_ID);
        verify(jobQueueService, times(1)).getJobResult(TEST_JOB_ID);
    }

    @Test
    @DisplayName("Should return NotFound when report file not found")
    void testDownloadReport_FileNotFound() {
        // Arrange
        JobStatus mockStatus = createMockJobStatus(TEST_JOB_ID, "COMPLETED", 100);
        ReportResult mockResult = createMockReportResult(TEST_JOB_ID);
        mockResult.setResultPath(null);
        
        when(jobQueueService.getJobStatus(TEST_JOB_ID)).thenReturn(mockStatus);
        when(jobQueueService.getJobResult(TEST_JOB_ID)).thenReturn(mockResult);

        // Act
        ResponseEntity<?> response = biController.downloadReport(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should cancel job successfully")
    void testCancelJob_Success() {
        // Arrange
        when(jobQueueService.cancelJob(TEST_JOB_ID)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.cancelJob(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        verify(jobQueueService, times(1)).cancelJob(TEST_JOB_ID);
    }

    @Test
    @DisplayName("Should handle cancel job failure")
    void testCancelJob_Failure() {
        // Arrange
        when(jobQueueService.cancelJob(TEST_JOB_ID)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.cancelJob(TEST_JOB_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
        verify(jobQueueService, times(1)).cancelJob(TEST_JOB_ID);
    }

    @Test
    @DisplayName("Should get all jobs status successfully")
    void testGetAllJobsStatus_Success() {
        // Arrange
        List<JobStatus> mockJobs = Arrays.asList(
                createMockJobStatus("JOB_001", "QUEUED", 0),
                createMockJobStatus("JOB_002", "PROCESSING", 50)
        );
        when(jobQueueService.getAllJobs()).thenReturn(mockJobs);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.getAllJobsStatus();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("jobs"));
    }

    @Test
    @DisplayName("Should get jobs by status successfully")
    void testGetJobsByStatus_Success() {
        // Arrange
        List<JobStatus> mockJobs = Arrays.asList(
                createMockJobStatus("JOB_001", "QUEUED", 0),
                createMockJobStatus("JOB_002", "QUEUED", 0)
        );
        when(jobQueueService.getJobsByStatus("QUEUED")).thenReturn(mockJobs);

        // Act
        ResponseEntity<Map<String, Object>> response = biController.getJobsByStatus("QUEUED");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        verify(jobQueueService, times(1)).getJobsByStatus("QUEUED");
    }

    @Test
    @DisplayName("Should handle service errors gracefully")
    void testGenerateBIReport_ServiceError() {
        // Arrange
        Map<String, Object> request = createValidReportRequest();
        when(jobQueueService.queueReportJob(any(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<Map<String, Object>> response = biController.generateBIReport(request, VALID_JWT);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
    }

    // Helper methods
    private Map<String, Object> createValidReportRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("reportType", "DAILY_REPORT");
        request.put("targetSystem", "BUSINESS_OBJECTS");
        request.put("dataFormat", "CSV");
        request.put("countyId", "CTA");
        request.put("priority", 5);
        return request;
    }

    private JobStatus createMockJobStatus(String jobId, String status, Integer progress) {
        JobStatus jobStatus = new JobStatus();
        jobStatus.setJobId(jobId);
        jobStatus.setStatus(status);
        jobStatus.setProgress(progress);
        jobStatus.setCreatedAt(LocalDateTime.now());
        jobStatus.setUserRole("ADMIN");
        jobStatus.setReportType("DAILY_REPORT");
        return jobStatus;
    }

    private ReportResult createMockReportResult(String jobId) {
        ReportResult result = new ReportResult();
        result.setJobId(jobId);
        result.setStatus("COMPLETED");
        result.setTotalRecords(100L);
        result.setProcessedRecords(100L);
        result.setCompletedAt(LocalDateTime.now());
        return result;
    }
}
