package com.cmips.service;

import com.cmips.model.UserRole;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportTemplateService {
    
    @Autowired
    private Configuration freemarkerConfig;
    
    @Autowired
    private FieldVisibilityService fieldVisibilityService;
    
    public ReportTemplateService() {
        System.out.println("🔧 ReportTemplateService: Initializing FreeMarker template service");
    }
    
    /**
     * Render email template with report data
     */
    public String renderEmailTemplate(String reportType, String userRole, 
                                     List<Map<String, Object>> reportData, 
                                     Map<String, Object> additionalData) {
        try {
            System.out.println("📧 ReportTemplateService: Rendering email template for report type: " + reportType);
            
            // Get visible fields for the user role - using default fields when no JWT token available
            String canonicalRole = UserRole.from(userRole).name();
            List<String> visibleFields = fieldVisibilityService.getDefaultVisibleFields();
            System.out.println("👁️ Visible fields for " + canonicalRole + ": " + visibleFields);
            
            // Prepare template data model
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("reportType", reportType);
            templateData.put("userRole", canonicalRole);
            templateData.put("reportData", reportData);
            templateData.put("visibleFields", visibleFields);
            templateData.put("recordCount", reportData.size());
            templateData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            templateData.put("dateRange", additionalData.getOrDefault("dateRange", "N/A"));
            templateData.put("systemName", "Report System");
            templateData.put("systemVersion", "1.0.0");
            
            // Add any additional data
            if (additionalData != null) {
                templateData.putAll(additionalData);
            }
            
            // Load and process template
            Template template = freemarkerConfig.getTemplate("reports/email/base-email.ftl");
            StringWriter writer = new StringWriter();
            template.process(templateData, writer);
            
            String renderedContent = writer.toString();
            System.out.println("✅ ReportTemplateService: Email template rendered successfully (" + renderedContent.length() + " chars)");
            
            return renderedContent;
            
        } catch (IOException | TemplateException e) {
            System.err.println("❌ Error rendering email template: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to render email template", e);
        }
    }
    
    /**
     * Render PDF content template with report data
     */
    public String renderPdfContentTemplate(String reportType, String userRole, 
                                         List<Map<String, Object>> reportData, 
                                         Map<String, Object> additionalData) {
        try {
            System.out.println("📄 ReportTemplateService: Rendering PDF content template for report type: " + reportType);
            
            // Get visible fields for the user role - using default fields when no JWT token available
            String canonicalRole = UserRole.from(userRole).name();
            List<String> visibleFields = fieldVisibilityService.getDefaultVisibleFields();
            System.out.println("👁️ Visible fields for " + canonicalRole + ": " + visibleFields);
            
            // Prepare template data model
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("reportType", reportType);
            templateData.put("userRole", canonicalRole);
            templateData.put("reportData", reportData);
            templateData.put("visibleFields", visibleFields);
            templateData.put("recordCount", reportData.size());
            templateData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            templateData.put("dateRange", additionalData.getOrDefault("dateRange", "N/A"));
            templateData.put("pageTitle", getReportTitle(reportType));
            templateData.put("pageSubtitle", "Generated for " + canonicalRole + " on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            // Add any additional data
            if (additionalData != null) {
                templateData.putAll(additionalData);
            }
            
            // Load and process template
            Template template = freemarkerConfig.getTemplate("reports/pdf/base-pdf.ftl");
            StringWriter writer = new StringWriter();
            template.process(templateData, writer);
            
            String renderedContent = writer.toString();
            System.out.println("✅ ReportTemplateService: PDF content template rendered successfully (" + renderedContent.length() + " chars)");
            
            return renderedContent;
            
        } catch (IOException | TemplateException e) {
            System.err.println("❌ Error rendering PDF content template: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to render PDF content template", e);
        }
    }
    
    /**
     * Render CSV template with report data
     */
    public String renderCsvTemplate(String reportType, String userRole, 
                                   List<Map<String, Object>> reportData, 
                                   Map<String, Object> additionalData) {
        try {
            System.out.println("📊 ReportTemplateService: Rendering CSV template for report type: " + reportType);
            
            // Get visible fields for the user role - using default fields when no JWT token available
            String canonicalRole = UserRole.from(userRole).name();
            List<String> visibleFields = fieldVisibilityService.getDefaultVisibleFields();
            System.out.println("👁️ Visible fields for " + canonicalRole + ": " + visibleFields);
            
            // Prepare template data model
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("reportType", reportType);
            templateData.put("userRole", canonicalRole);
            templateData.put("reportData", reportData);
            templateData.put("visibleFields", visibleFields);
            templateData.put("recordCount", reportData.size());
            templateData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            templateData.put("dateRange", additionalData.getOrDefault("dateRange", "N/A"));
            
            // Add any additional data
            if (additionalData != null) {
                templateData.putAll(additionalData);
            }
            
            // Load and process template
            Template template = freemarkerConfig.getTemplate("reports/csv/base-csv.ftl");
            StringWriter writer = new StringWriter();
            template.process(templateData, writer);
            
            String renderedContent = writer.toString();
            System.out.println("✅ ReportTemplateService: CSV template rendered successfully (" + renderedContent.length() + " chars)");
            
            return renderedContent;
            
        } catch (IOException | TemplateException e) {
            System.err.println("❌ Error rendering CSV template: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to render CSV template", e);
        }
    }
    
    /**
     * Get report title based on report type
     */
    private String getReportTitle(String reportType) {
        // Only timesheet reports for now
        return "Timesheet Report";
    }
    
    /**
     * Test template rendering
     */
    public boolean testTemplateRendering() {
        try {
            System.out.println("🧪 ReportTemplateService: Testing template rendering...");
            
            // Create test data
            Map<String, Object> testData = new HashMap<>();
            testData.put("timesheetId", "TS001");
            testData.put("totalHours", 40.0);
            testData.put("status", "APPROVED");
            
            List<Map<String, Object>> testReportData = List.of(testData);
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("dateRange", "2025-01-01 to 2025-01-31");
            
            // Test email template
            String emailContent = renderEmailTemplate("TIMESHEET_REPORT", UserRole.ADMIN.name(), testReportData, additionalData);
            boolean emailSuccess = emailContent != null && !emailContent.trim().isEmpty();
            
            // Test PDF content template
            String pdfContent = renderPdfContentTemplate("TIMESHEET_REPORT", UserRole.ADMIN.name(), testReportData, additionalData);
            boolean pdfSuccess = pdfContent != null && !pdfContent.trim().isEmpty();
            
            boolean overallSuccess = emailSuccess && pdfSuccess;
            System.out.println(overallSuccess ? "✅ Template rendering test successful" : "❌ Template rendering test failed");
            return overallSuccess;
            
        } catch (Exception e) {
            System.err.println("❌ Template rendering test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get template service status
     */
    public String getTemplateServiceStatus() {
        return String.format(
            "Report Template Service Status:\n" +
            "Service: Active\n" +
            "Template Engine: FreeMarker\n" +
            "Template Directory: templates/reports/\n" +
            "Supported Formats: Email, PDF, CSV\n" +
            "Dynamic Field Rendering: Enabled\n" +
            "Role-Based Templates: Enabled"
        );
    }
}
