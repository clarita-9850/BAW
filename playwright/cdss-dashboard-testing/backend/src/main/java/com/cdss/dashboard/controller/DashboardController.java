package com.cdss.dashboard.controller;

import com.cdss.dashboard.dto.DashboardFilterRequest;
import com.cdss.dashboard.dto.DashboardResponse;
import com.cdss.dashboard.dto.DashboardResponse.FilterOptions;
import com.cdss.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PostMapping("/data")
    public ResponseEntity<DashboardResponse> getDashboardData(
            @RequestBody(required = false) DashboardFilterRequest request) {
        if (request == null) {
            request = new DashboardFilterRequest();
        }
        return ResponseEntity.ok(dashboardService.getDashboardData(request));
    }

    @GetMapping("/data")
    public ResponseEntity<DashboardResponse> getDashboardDataGet() {
        return ResponseEntity.ok(dashboardService.getDashboardData(new DashboardFilterRequest()));
    }

    @GetMapping("/filters")
    public ResponseEntity<FilterOptions> getFilterOptions() {
        return ResponseEntity.ok(dashboardService.getFilterOptions());
    }
}
