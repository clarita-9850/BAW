package com.cmips.service;

import com.cmips.entity.EVVRecord;
import com.cmips.repository.EVVRepository;
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
 * Unit tests for EVVService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EVVService Tests")
class EVVServiceTest {

    @Mock
    private EVVRepository evvRepository;

    @InjectMocks
    private EVVService evvService;

    private static final String TEST_PROVIDER_ID = "provider1";
    private static final String TEST_RECIPIENT_ID = "recipient1";
    private static final Double TEST_LATITUDE = 34.0522;
    private static final Double TEST_LONGITUDE = -118.2437;

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should check-in successfully")
    void testCheckIn_Success() {
        // Arrange
        when(evvRepository.findByProviderIdAndRecipientIdAndStatus(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(evvRepository.save(any(EVVRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EVVRecord result = evvService.checkIn(TEST_PROVIDER_ID, TEST_RECIPIENT_ID, "Personal Care", 
                TEST_LATITUDE, TEST_LONGITUDE);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PROVIDER_ID, result.getProviderId());
        assertEquals(TEST_RECIPIENT_ID, result.getRecipientId());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertNotNull(result.getCheckInTime());
        verify(evvRepository, times(1)).save(any(EVVRecord.class));
    }

    @Test
    @DisplayName("Should throw exception when active check-in exists")
    void testCheckIn_ActiveCheckInExists() {
        // Arrange
        EVVRecord activeCheckIn = createMockEVVRecord(1L);
        when(evvRepository.findByProviderIdAndRecipientIdAndStatus(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(activeCheckIn));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
                evvService.checkIn(TEST_PROVIDER_ID, TEST_RECIPIENT_ID, "Personal Care", 
                        TEST_LATITUDE, TEST_LONGITUDE));
        verify(evvRepository, never()).save(any(EVVRecord.class));
    }

    @Test
    @DisplayName("Should check-out successfully")
    void testCheckOut_Success() {
        // Arrange
        EVVRecord evvRecord = createMockEVVRecord(1L);
        evvRecord.setStatus("IN_PROGRESS");
        evvRecord.setCheckInTime(LocalDateTime.now().minusHours(2));
        
        when(evvRepository.findById(1L)).thenReturn(Optional.of(evvRecord));
        when(evvRepository.save(any(EVVRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EVVRecord result = evvService.checkOut(1L, TEST_LATITUDE, TEST_LONGITUDE);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCheckOutTime());
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getHoursWorked());
        verify(evvRepository, times(1)).save(any(EVVRecord.class));
    }

    @Test
    @DisplayName("Should detect location violation on check-out")
    void testCheckOut_LocationViolation() {
        // Arrange
        EVVRecord evvRecord = createMockEVVRecord(1L);
        evvRecord.setStatus("IN_PROGRESS");
        evvRecord.setCheckInTime(LocalDateTime.now().minusHours(2));
        evvRecord.setCheckInLatitude(34.0522);
        evvRecord.setCheckInLongitude(-118.2437);
        
        // Check-out location far away (>1km)
        Double farLatitude = 34.1000;
        Double farLongitude = -118.3000;
        
        when(evvRepository.findById(1L)).thenReturn(Optional.of(evvRecord));
        when(evvRepository.save(any(EVVRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EVVRecord result = evvService.checkOut(1L, farLatitude, farLongitude);

        // Assert
        assertNotNull(result);
        assertEquals("VIOLATION", result.getStatus());
        assertEquals("LOCATION_MISMATCH", result.getViolationType());
        verify(evvRepository, times(1)).save(any(EVVRecord.class));
    }

    @Test
    @DisplayName("Should throw exception when EVV record not found for check-out")
    void testCheckOut_NotFound() {
        // Arrange
        when(evvRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                evvService.checkOut(999L, TEST_LATITUDE, TEST_LONGITUDE));
    }

    @Test
    @DisplayName("Should throw exception when check-out on inactive session")
    void testCheckOut_InactiveSession() {
        // Arrange
        EVVRecord evvRecord = createMockEVVRecord(1L);
        evvRecord.setStatus("COMPLETED");
        
        when(evvRepository.findById(1L)).thenReturn(Optional.of(evvRecord));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
                evvService.checkOut(1L, TEST_LATITUDE, TEST_LONGITUDE));
    }

    @Test
    @DisplayName("Should get provider EVV records successfully")
    void testGetProviderEVVRecords_Success() {
        // Arrange
        List<EVVRecord> mockRecords = Arrays.asList(createMockEVVRecord(1L), createMockEVVRecord(2L));
        when(evvRepository.findByProviderId(TEST_PROVIDER_ID)).thenReturn(mockRecords);

        // Act
        List<EVVRecord> result = evvService.getProviderEVVRecords(TEST_PROVIDER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(evvRepository, times(1)).findByProviderId(TEST_PROVIDER_ID);
    }

    @Test
    @DisplayName("Should get active check-in successfully")
    void testGetActiveCheckIn_Success() {
        // Arrange
        EVVRecord activeCheckIn = createMockEVVRecord(1L);
        activeCheckIn.setStatus("IN_PROGRESS");
        when(evvRepository.findByProviderIdAndRecipientIdAndStatus(TEST_PROVIDER_ID, TEST_RECIPIENT_ID, "IN_PROGRESS"))
                .thenReturn(Optional.of(activeCheckIn));

        // Act
        Optional<EVVRecord> result = evvService.getActiveCheckIn(TEST_PROVIDER_ID, TEST_RECIPIENT_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("IN_PROGRESS", result.get().getStatus());
    }

    // Helper methods
    private EVVRecord createMockEVVRecord(Long id) {
        EVVRecord record = new EVVRecord();
        record.setId(id);
        record.setProviderId(TEST_PROVIDER_ID);
        record.setRecipientId(TEST_RECIPIENT_ID);
        record.setServiceType("Personal Care");
        record.setCheckInTime(LocalDateTime.now());
        record.setCheckInLatitude(TEST_LATITUDE);
        record.setCheckInLongitude(TEST_LONGITUDE);
        record.setStatus("IN_PROGRESS");
        return record;
    }
}







