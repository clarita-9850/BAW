package com.cmips.controller;

import com.cmips.entity.ProviderRecipientRelationship;
import com.cmips.repository.ProviderRecipientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProviderRecipientController
 * 
 * Tests cover:
 * - Get provider's recipients
 * - Get recipient's providers
 * - Get all relationships
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProviderRecipientController Tests")
class ProviderRecipientControllerTest {

    @Mock
    private ProviderRecipientRepository providerRecipientRepository;

    @InjectMocks
    private ProviderRecipientController providerRecipientController;

    private static final String TEST_PROVIDER_ID = "provider1";
    private static final String TEST_RECIPIENT_ID = "recipient1";

    @BeforeEach
    void setUp() {
        // Security context setup is done per test as needed
    }

    @Test
    @DisplayName("Should get my recipients successfully (PROVIDER role)")
    void testGetMyRecipients_Success() {
        // Arrange
        List<ProviderRecipientRelationship> mockRelationships = createMockRelationships();
        when(providerRecipientRepository.findByProviderIdAndStatus(eq(TEST_PROVIDER_ID), eq("ACTIVE")))
                .thenReturn(mockRelationships);
        setupSecurityContextForProvider();

        // Act
        ResponseEntity<List<ProviderRecipientRelationship>> response = providerRecipientController.getMyRecipients();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(providerRecipientRepository, times(1)).findByProviderIdAndStatus(eq(TEST_PROVIDER_ID), eq("ACTIVE"));
    }

    @Test
    @DisplayName("Should get my providers successfully (RECIPIENT role)")
    void testGetMyProviders_Success() {
        // Arrange
        List<ProviderRecipientRelationship> mockRelationships = createMockRelationships();
        when(providerRecipientRepository.findByRecipientIdAndStatus(eq(TEST_RECIPIENT_ID), eq("ACTIVE")))
                .thenReturn(mockRelationships);
        setupSecurityContextForRecipient();

        // Act
        ResponseEntity<List<ProviderRecipientRelationship>> response = providerRecipientController.getMyProviders();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(providerRecipientRepository, times(1)).findByRecipientIdAndStatus(eq(TEST_RECIPIENT_ID), eq("ACTIVE"));
    }

    @Test
    @DisplayName("Should get all relationships successfully (CASE_WORKER role)")
    void testGetAllRelationships_Success() {
        // Arrange
        List<ProviderRecipientRelationship> mockRelationships = createMockRelationships();
        when(providerRecipientRepository.findAll()).thenReturn(mockRelationships);

        // Act
        ResponseEntity<List<ProviderRecipientRelationship>> response = providerRecipientController.getAllRelationships();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(providerRecipientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no relationships found")
    void testGetMyRecipients_Empty() {
        // Arrange
        when(providerRecipientRepository.findByProviderIdAndStatus(eq(TEST_PROVIDER_ID), eq("ACTIVE")))
                .thenReturn(Arrays.asList());
        setupSecurityContextForProvider();

        // Act
        ResponseEntity<List<ProviderRecipientRelationship>> response = providerRecipientController.getMyRecipients();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    // Helper methods
    private void setupSecurityContextForProvider() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(TEST_PROVIDER_ID);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupSecurityContextForRecipient() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(TEST_RECIPIENT_ID);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private List<ProviderRecipientRelationship> createMockRelationships() {
        ProviderRecipientRelationship rel1 = new ProviderRecipientRelationship();
        rel1.setId(1L);
        rel1.setProviderId(TEST_PROVIDER_ID);
        rel1.setRecipientId("recipient1");
        rel1.setStatus("ACTIVE");

        ProviderRecipientRelationship rel2 = new ProviderRecipientRelationship();
        rel2.setId(2L);
        rel2.setProviderId(TEST_PROVIDER_ID);
        rel2.setRecipientId("recipient2");
        rel2.setStatus("ACTIVE");

        return Arrays.asList(rel1, rel2);
    }
}

