package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.service.DistrictCountyService;
import com.example.kafkaeventdrivenapp.service.CountyMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CountyAccessControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CountyAccessController controller = new CountyAccessController();
        CountyMappingService countyMappingService = new CountyMappingService();
        ReflectionTestUtils.setField(controller, "districtCountyService", new DistrictCountyService(countyMappingService));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Removed getAllDistrictsReturnsSuccessPayload test - endpoint /api/district-county/districts doesn't exist

    @Test
    void checkCountyAccessReturnsBoolean() throws Exception {
        mockMvc.perform(get("/api/county/access-check/ADMIN/Alameda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canAccess").value(true))
                .andExpect(jsonPath("$.countyName").value("Alameda"));
    }

    @Test
    void getAccessibleCountiesReturnsList() throws Exception {
        mockMvc.perform(get("/api/county/accessible-counties/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessibleCounties").isArray())
                .andExpect(jsonPath("$.totalCounties").exists());
    }
}

