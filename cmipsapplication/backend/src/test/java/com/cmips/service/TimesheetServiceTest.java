package com.cmips.service;

import com.cmips.dto.TimesheetCreateRequest;
import com.cmips.dto.TimesheetResponse;
import com.cmips.dto.TimesheetUpdateRequest;
import com.cmips.entity.Timesheet;
import com.cmips.entity.TimesheetStatus;
import com.cmips.repository.TimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimesheetService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetService Tests")
class TimesheetServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @InjectMocks
    private TimesheetService timesheetService;

    private static final String TEST_USER_ID = "user123";
    private static final LocalDate TEST_PAY_PERIOD_START = LocalDate.now().minusDays(14);
    private static final LocalDate TEST_PAY_PERIOD_END = LocalDate.now();

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should create timesheet successfully")
    void testCreateTimesheet_Success() {
        // Arrange
        TimesheetCreateRequest request = createValidTimesheetRequest();
        when(timesheetRepository.findByUserIdAndPayPeriodStartAndPayPeriodEnd(anyString(), any(), any()))
                .thenReturn(Optional.empty());
        when(timesheetRepository.save(any(Timesheet.class))).thenAnswer(invocation -> {
            Timesheet ts = invocation.getArgument(0);
            ts.setId(1L);
            return ts;
        });

        // Act
        TimesheetResponse result = timesheetService.createTimesheet(TEST_USER_ID, request);

        // Assert
        assertNotNull(result);
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    @DisplayName("Should throw exception when timesheet already exists for pay period")
    void testCreateTimesheet_DuplicatePayPeriod() {
        // Arrange
        TimesheetCreateRequest request = createValidTimesheetRequest();
        Timesheet existing = createMockTimesheet(1L);
        when(timesheetRepository.findByUserIdAndPayPeriodStartAndPayPeriodEnd(anyString(), any(), any()))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                timesheetService.createTimesheet(TEST_USER_ID, request));
        verify(timesheetRepository, never()).save(any(Timesheet.class));
    }

    @Test
    @DisplayName("Should get timesheet by ID successfully")
    void testGetTimesheetById_Success() {
        // Arrange
        Long timesheetId = 1L;
        Timesheet mockTimesheet = createMockTimesheet(timesheetId);
        when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.of(mockTimesheet));

        // Act
        Optional<TimesheetResponse> result = timesheetService.getTimesheetById(timesheetId);

        // Assert
        assertTrue(result.isPresent());
        verify(timesheetRepository, times(1)).findById(timesheetId);
    }

    @Test
    @DisplayName("Should get timesheets by user ID successfully")
    void testGetTimesheetsByUserId_Success() {
        // Arrange
        List<Timesheet> mockTimesheets = Arrays.asList(
                createMockTimesheet(1L),
                createMockTimesheet(2L)
        );
        when(timesheetRepository.findByUserIdOrderByPayPeriodStartDesc(TEST_USER_ID))
                .thenReturn(mockTimesheets);

        // Act
        List<TimesheetResponse> result = timesheetService.getTimesheetsByUserId(TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(timesheetRepository, times(1)).findByUserIdOrderByPayPeriodStartDesc(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should submit timesheet successfully")
    void testSubmitTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        Timesheet timesheet = createMockTimesheet(timesheetId);
        timesheet.setStatus(TimesheetStatus.DRAFT);
        
        when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.of(timesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<TimesheetResponse> result = timesheetService.submitTimesheet(timesheetId, TEST_USER_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TimesheetStatus.SUBMITTED, timesheet.getStatus());
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    @DisplayName("Should throw exception when submitting non-draft timesheet")
    void testSubmitTimesheet_InvalidStatus() {
        // Arrange
        Long timesheetId = 1L;
        Timesheet timesheet = createMockTimesheet(timesheetId);
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        
        when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.of(timesheet));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
                timesheetService.submitTimesheet(timesheetId, TEST_USER_ID));
    }

    @Test
    @DisplayName("Should approve timesheet successfully")
    void testApproveTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        String supervisorId = "supervisor1";
        Timesheet timesheet = createMockTimesheet(timesheetId);
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        
        when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.of(timesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<TimesheetResponse> result = timesheetService.approveTimesheet(timesheetId, supervisorId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TimesheetStatus.APPROVED, timesheet.getStatus());
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    @DisplayName("Should reject timesheet successfully")
    void testRejectTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        String supervisorId = "supervisor1";
        String comments = "Incorrect hours";
        Timesheet timesheet = createMockTimesheet(timesheetId);
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        
        when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.of(timesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<TimesheetResponse> result = timesheetService.rejectTimesheet(timesheetId, supervisorId, comments);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TimesheetStatus.REJECTED, timesheet.getStatus());
        assertEquals(comments, timesheet.getSupervisorComments());
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    @DisplayName("Should delete draft timesheet successfully")
    void testDeleteTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        Timesheet timesheet = createMockTimesheet(timesheetId);
        timesheet.setStatus(TimesheetStatus.DRAFT);
        
        when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.of(timesheet));
        doNothing().when(timesheetRepository).deleteById(timesheetId);

        // Act
        boolean result = timesheetService.deleteTimesheet(timesheetId, TEST_USER_ID);

        // Assert
        assertTrue(result);
        verify(timesheetRepository, times(1)).deleteById(timesheetId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-draft timesheet")
    void testDeleteTimesheet_InvalidStatus() {
        // Arrange
        Long timesheetId = 1L;
        Timesheet timesheet = createMockTimesheet(timesheetId);
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        
        when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.of(timesheet));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
                timesheetService.deleteTimesheet(timesheetId, TEST_USER_ID));
        verify(timesheetRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get timesheets by status successfully")
    void testGetTimesheetsByStatus_Success() {
        // Arrange
        List<Timesheet> mockTimesheets = Arrays.asList(createMockTimesheet(1L));
        when(timesheetRepository.findByStatusOrderByCreatedAtDesc(TimesheetStatus.SUBMITTED))
                .thenReturn(mockTimesheets);

        // Act
        List<TimesheetResponse> result = timesheetService.getTimesheetsByStatus(TimesheetStatus.SUBMITTED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(timesheetRepository, times(1)).findByStatusOrderByCreatedAtDesc(TimesheetStatus.SUBMITTED);
    }

    @Test
    @DisplayName("Should get all timesheets with pagination successfully")
    void testGetAllTimesheets_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Timesheet> mockTimesheets = Arrays.asList(createMockTimesheet(1L));
        Page<Timesheet> mockPage = new PageImpl<>(mockTimesheets, pageable, 1);
        when(timesheetRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        Page<TimesheetResponse> result = timesheetService.getAllTimesheets(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(timesheetRepository, times(1)).findAll(pageable);
    }

    // Helper methods
    private TimesheetCreateRequest createValidTimesheetRequest() {
        TimesheetCreateRequest request = new TimesheetCreateRequest();
        request.setEmployeeId("EMP001");
        request.setEmployeeName("John Doe");
        request.setDepartment("IT");
        request.setLocation("CTA");
        request.setPayPeriodStart(TEST_PAY_PERIOD_START);
        request.setPayPeriodEnd(TEST_PAY_PERIOD_END);
        request.setRegularHours(BigDecimal.valueOf(40.0));
        request.setOvertimeHours(BigDecimal.ZERO);
        return request;
    }

    private Timesheet createMockTimesheet(Long id) {
        Timesheet timesheet = new Timesheet(
                TEST_USER_ID,
                "EMP001",
                "John Doe",
                "IT",
                "CTA",
                TEST_PAY_PERIOD_START,
                TEST_PAY_PERIOD_END
        );
        timesheet.setId(id);
        timesheet.setRegularHours(BigDecimal.valueOf(40.0));
        timesheet.setStatus(TimesheetStatus.DRAFT);
        timesheet.setCreatedAt(LocalDateTime.now());
        timesheet.calculateTotalHours();
        return timesheet;
    }
}







