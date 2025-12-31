package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.ReportGenerationRequest;
import com.example.kafkaeventdrivenapp.model.ReportGenerationResponse;
import com.example.kafkaeventdrivenapp.service.EmailReportService;
import com.example.kafkaeventdrivenapp.service.PDFReportGeneratorService;
import com.example.kafkaeventdrivenapp.service.ReportGenerationService;
import com.example.kafkaeventdrivenapp.service.ScheduledReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportDeliveryController {
    
    @Autowired
    private ReportGenerationService reportGenerationService;
    
    @Autowired
    private PDFReportGeneratorService pdfReportGeneratorService;
    
    @Autowired
    private EmailReportService emailReportService;
    
    @Autowired
    private ScheduledReportService scheduledReportService;
    
    @Autowired
    private com.example.kafkaeventdrivenapp.config.ReportTypeProperties reportTypeProperties;
    
    public ReportDeliveryController() {
        System.out.println("🔧 ReportDeliveryController: Initializing report delivery controller");
    }
    
    /**
     * Generate and email report immediately
     */
    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> generateAndEmailReport(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📧 ReportDeliveryController: Generating and emailing report...");
            
            // Extract JWT token from Authorization header
            String jwtToken = null;
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                System.out.println("🔐 ReportDeliveryController: JWT token extracted from request");
            }
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "JWT token is required for report generation");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extract request parameters
            String userRole = (String) request.get("userRole");
            String reportType = (String) request.getOrDefault("reportType", reportTypeProperties.getDefaultReportType());
            String startDate = (String) request.get("startDate");
            String endDate = (String) request.get("endDate");
            String countyId = (String) request.get("countyId");
            @SuppressWarnings("unchecked")
            List<String> recipients = (List<String>) request.get("recipients");
            
            if (userRole == null) {
                response.put("status", "ERROR");
                response.put("message", "userRole is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create report generation request
            ReportGenerationRequest reportRequest = new ReportGenerationRequest();
            reportRequest.setUserRole(userRole);
            reportRequest.setReportType(reportType);
            reportRequest.setUserCounty(countyId);
            if (startDate != null) {
                reportRequest.setStartDate(LocalDate.parse(startDate));
            }
            if (endDate != null) {
                reportRequest.setEndDate(LocalDate.parse(endDate));
            }
            
            // Generate report
            ReportGenerationResponse reportResponse = reportGenerationService.generateReport(reportRequest, jwtToken);
            
            if (!"SUCCESS".equals(reportResponse.getStatus())) {
                response.put("status", "ERROR");
                response.put("message", "Report generation failed: " + reportResponse.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Prepare additional data for email
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("dateRange", startDate + " to " + endDate);
            additionalData.put("isManual", true);
            additionalData.put("requestedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Send email
            boolean emailSuccess;
            if (recipients != null && !recipients.isEmpty()) {
                emailSuccess = emailReportService.sendReportEmail(reportType, userRole, reportResponse.getData().getRecords(), additionalData, recipients, jwtToken);
            } else {
                emailSuccess = emailReportService.sendScheduledReportEmail(reportType, userRole, reportResponse.getData().getRecords(), additionalData, jwtToken);
            }
            
            if (emailSuccess) {
                response.put("status", "SUCCESS");
                response.put("message", "Report generated and emailed successfully");
                response.put("reportId", reportResponse.getReportId());
                response.put("emailSent", true);
                response.put("recipients", recipients != null ? recipients : "default recipients");
            } else {
                response.put("status", "ERROR");
                response.put("message", "Report generated but email failed");
                response.put("reportId", reportResponse.getReportId());
                response.put("emailSent", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error in generateAndEmailReport: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to generate and email report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Generate PDF and return download link
     */
    @PostMapping("/generate-pdf")
    public ResponseEntity<?> generatePDFReport(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📄 ReportDeliveryController: Generating PDF report...");
            
            // Extract JWT token from Authorization header
            String jwtToken = null;
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                System.out.println("🔐 ReportDeliveryController: JWT token extracted from request");
            }
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "JWT token is required for report generation");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extract request parameters
            String userRole = (String) request.get("userRole");
            String reportType = (String) request.getOrDefault("reportType", reportTypeProperties.getDefaultReportType());
            String startDate = (String) request.get("startDate");
            String endDate = (String) request.get("endDate");
            String countyId = (String) request.get("countyId");
            
            if (userRole == null) {
                response.put("status", "ERROR");
                response.put("message", "userRole is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create report generation request
            ReportGenerationRequest reportRequest = new ReportGenerationRequest();
            reportRequest.setUserRole(userRole);
            reportRequest.setReportType(reportType);
            reportRequest.setUserCounty(countyId);
            if (startDate != null) {
                reportRequest.setStartDate(LocalDate.parse(startDate));
            }
            if (endDate != null) {
                reportRequest.setEndDate(LocalDate.parse(endDate));
            }
            
            // Generate report
            ReportGenerationResponse reportResponse = reportGenerationService.generateReport(reportRequest, jwtToken);
            
            if (!"SUCCESS".equals(reportResponse.getStatus())) {
                response.put("status", "ERROR");
                response.put("message", "Report generation failed: " + reportResponse.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Prepare additional data for PDF
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("dateRange", startDate + " to " + endDate);
            additionalData.put("isManual", true);
            additionalData.put("requestedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Generate PDF with JWT token for proper field masking
            byte[] pdfBytes = pdfReportGeneratorService.generatePDFReport(reportType, userRole, reportResponse.getData().getRecords(), additionalData, jwtToken);
            
            // Return PDF as download
            String filename = String.format("%s_%s_%s.pdf", 
                reportType.toLowerCase(), 
                userRole.toLowerCase(), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
            
        } catch (Exception e) {
            System.err.println("❌ Error in generatePDFReport: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to generate PDF report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * View scheduled report status
     */
    @GetMapping("/scheduled/status")
    public ResponseEntity<Map<String, Object>> getScheduledReportStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📊 ReportDeliveryController: Getting scheduled report status...");
            
            String status = scheduledReportService.getScheduledReportServiceStatus();
            
            response.put("status", "SUCCESS");
            response.put("message", "Scheduled report status retrieved");
            response.put("serviceStatus", status);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting scheduled report status: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get scheduled report status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Manually trigger scheduled report
     */
    @PostMapping("/scheduled/trigger")
    public ResponseEntity<Map<String, Object>> triggerScheduledReport(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔧 ReportDeliveryController: Manually triggering scheduled report...");
            
            // Extract JWT token from Authorization header
            String jwtToken = null;
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                System.out.println("🔐 ReportDeliveryController: JWT token extracted from request");
            }
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "JWT token is required for scheduled report triggering");
                return ResponseEntity.badRequest().body(response);
            }
            
            String userRole = (String) request.get("userRole");
            String reportType = (String) request.getOrDefault("reportType", reportTypeProperties.getDefaultReportType());
            
            if (userRole == null) {
                response.put("status", "ERROR");
                response.put("message", "userRole is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = scheduledReportService.triggerScheduledReport(userRole, reportType, jwtToken);
            
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Scheduled report triggered successfully");
                response.put("userRole", userRole);
                response.put("reportType", reportType);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Failed to trigger scheduled report");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error triggering scheduled report: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to trigger scheduled report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    
    

    private String extractJwtToken(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
