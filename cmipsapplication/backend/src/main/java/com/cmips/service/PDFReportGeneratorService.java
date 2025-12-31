package com.cmips.service;

import com.cmips.model.UserRole;
import com.cmips.repository.TimesheetRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.awt.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PDFReportGeneratorService {
    
    @Autowired
    private FieldVisibilityService fieldVisibilityService;
    
    @Autowired
    private TimesheetRepository timesheetRepository;
    
    public PDFReportGeneratorService() {
        System.out.println("🔧 PDFReportGeneratorService: Initializing PDF report generator");
    }
    
    /**
     * Generate PDF report from report data
     */
    public byte[] generatePDFReport(String reportType, String userRole, 
                                   List<Map<String, Object>> reportData, 
                                   Map<String, Object> additionalData) {
        System.out.println("🔧 PDFReportGeneratorService: generatePDFReport called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use generatePDFReport(reportType, userRole, reportData, additionalData, jwtToken) with JWT token.");
    }
    
    public byte[] generatePDFReport(String reportType, String userRole, 
                                   List<Map<String, Object>> reportData, 
                                   Map<String, Object> additionalData, String jwtToken) {
        try {
            System.out.println("📄 PDFReportGeneratorService: Generating PDF report for type: " + reportType + ", role: " + userRole);
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token is required for PDF generation. No fallback methods available.");
            }
            
            // Get visible fields using JWT token for proper role-based field masking
            List<String> visibleFields = fieldVisibilityService.getVisibleFields(userRole, jwtToken);
            System.out.println("👁️ Visible fields for " + userRole + " (from JWT): " + visibleFields);
            
            // Create PDF document
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            
            // Add metadata
            document.addTitle(getReportTitle(reportType));
            document.addSubject("Generated for " + userRole);
            document.addCreator("Report System");
            document.addCreationDate();
            
            document.open();
            
            // Extract date range from additionalData for analytics queries
            String dateRangeStr = (String) additionalData.getOrDefault("dateRange", "");
            LocalDate startDate = extractStartDate(additionalData);
            LocalDate endDate = extractEndDate(additionalData);
            String county = (String) additionalData.getOrDefault("userCounty", null);
            String districtId = (String) additionalData.getOrDefault("districtId", null);
            
            // Enhanced report structure
            // 1. Cover Page
            addCoverPage(document, reportType, userRole, additionalData);
            document.newPage();
            
            // 2. Executive Summary
            addExecutiveSummary(document, reportData, userRole, jwtToken, startDate, endDate, county, districtId);
            document.newPage();
            
            // 3. Analytics & Insights Section
            addAnalyticsSection(document, reportData, userRole, jwtToken, startDate, endDate, county, districtId);
            document.newPage();
            
            // 4. Detailed Data Section
            addDetailedDataSection(document, reportData, visibleFields, userRole);
            
            // 5. Appendices
            addAppendices(document, reportData, userRole, jwtToken, additionalData);
            
            // Add footer
            addReportFooter(document, userRole);
            
            document.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            System.out.println("✅ PDFReportGeneratorService: PDF report generated successfully (" + pdfBytes.length + " bytes)");
            
            return pdfBytes;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating PDF report: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
    
    /**
     * Generate PDF report and save to file
     */
    public String generatePDFReportToFile(String reportType, String userRole, 
                                         List<Map<String, Object>> reportData, 
                                         Map<String, Object> additionalData, 
                                         String filePath, String jwtToken) {
        try {
            System.out.println("📄 PDFReportGeneratorService: Generating PDF report to file: " + filePath);
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token is required for PDF generation");
            }
            
            byte[] pdfBytes = generatePDFReport(reportType, userRole, reportData, additionalData, jwtToken);
            
            // Write to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }
            
            System.out.println("✅ PDFReportGeneratorService: PDF report saved to file: " + filePath + " (" + pdfBytes.length + " bytes)");
            return filePath;
            
        } catch (Exception e) {
            System.err.println("❌ Error saving PDF report to file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save PDF report to file", e);
        }
    }
    
    /**
     * Add report header to PDF
     */
    private void addReportHeader(Document document, String reportType, String userRole, Map<String, Object> additionalData) throws DocumentException {
        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
        Paragraph title = new Paragraph(getReportTitle(reportType), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Subtitle
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
        Paragraph subtitle = new Paragraph("Generated for " + userRole, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);
        
        // Date range
        String dateRange = (String) additionalData.getOrDefault("dateRange", "N/A");
        Paragraph dateRangePara = new Paragraph("Date Range: " + dateRange, subtitleFont);
        dateRangePara.setAlignment(Element.ALIGN_CENTER);
        dateRangePara.setSpacingAfter(20);
        document.add(dateRangePara);
        
        // Generated timestamp
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Paragraph timestamp = new Paragraph("Generated: " + generatedAt, subtitleFont);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        timestamp.setSpacingAfter(30);
        document.add(timestamp);
        
        // Add line separator
        LineSeparator line = new LineSeparator();
        document.add(new Chunk(line));
        document.add(new Paragraph(" "));
    }
    
    /**
     * Add data table to PDF
     */
    private void addDataTable(Document document, List<Map<String, Object>> reportData, 
                             List<String> visibleFields, String userRole) throws DocumentException {
        if (reportData == null || reportData.isEmpty()) {
            Paragraph noData = new Paragraph("No data available for this report.", FontFactory.getFont(FontFactory.HELVETICA, 10));
            document.add(noData);
            return;
        }
        
        // Create table with visible fields
        PdfPTable table = new PdfPTable(visibleFields.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        // Set column widths
        float[] columnWidths = new float[visibleFields.size()];
        for (int i = 0; i < visibleFields.size(); i++) {
            columnWidths[i] = 1.0f;
        }
        table.setWidths(columnWidths);
        
        // Add header row
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        
        for (String field : visibleFields) {
            PdfPCell headerCell = new PdfPCell(new Phrase(field, headerFont));
            headerCell.setBackgroundColor(Color.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(5);
            table.addCell(headerCell);
        }
        
        // Add data rows
        for (Map<String, Object> record : reportData) {
            for (String field : visibleFields) {
                Object value = record.get(field);
                String cellValue = (value != null) ? value.toString() : "";
                
                // Apply field masking based on role
                if (shouldMaskField(field, userRole)) {
                    cellValue = maskFieldValue(field, cellValue);
                }
                
                PdfPCell cell = new PdfPCell(new Phrase(cellValue, cellFont));
                cell.setPadding(3);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);
            }
        }
        
        document.add(table);
    }
    
    /**
     * Add report summary to PDF
     */
    private void addReportSummary(Document document, List<Map<String, Object>> reportData, String userRole, String jwtToken) throws DocumentException {
        document.add(new Paragraph(" "));
        
        // Summary section
        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Paragraph summaryTitle = new Paragraph("Report Summary", summaryFont);
        summaryTitle.setSpacingBefore(20);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);
        
        // Record count
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph recordCount = new Paragraph("Total Records: " + (reportData != null ? reportData.size() : 0), dataFont);
        recordCount.setSpacingAfter(5);
        document.add(recordCount);
        
        // User role
        Paragraph userRolePara = new Paragraph("Generated for Role: " + userRole, dataFont);
        userRolePara.setSpacingAfter(5);
        document.add(userRolePara);
        
        // Field visibility info using JWT token
        List<String> visibleFields = fieldVisibilityService.getVisibleFields(userRole, jwtToken);
        Paragraph fieldInfo = new Paragraph("Visible Fields: " + visibleFields.size() + " (" + String.join(", ", visibleFields) + ")", dataFont);
        fieldInfo.setSpacingAfter(10);
        document.add(fieldInfo);
    }
    
    /**
     * Add report footer to PDF
     */
    private void addReportFooter(Document document, String userRole) throws DocumentException {
        document.add(new Paragraph(" "));
        
        // Footer line
        LineSeparator line = new LineSeparator();
        document.add(new Chunk(line));
        
        // Footer text
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Paragraph footer = new Paragraph("Report System v1.0 | Generated for " + userRole + " | " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);
    }
    
    /**
     * Check if field should be masked based on user role
     */
    private boolean shouldMaskField(String fieldName, String userRole) {
        UserRole role = UserRole.from(userRole);
        String lowerField = fieldName.toLowerCase();
        return switch (role) {
            case ADMIN, SYSTEM_SCHEDULER -> false;
            case SUPERVISOR -> lowerField.contains("email") || lowerField.contains("phone");
            case CASE_WORKER -> lowerField.contains("email") || lowerField.contains("phone") || lowerField.contains("address");
            case PROVIDER, RECIPIENT -> true;
        };
    }
    
    /**
     * Mask field value based on field type
     */
    private String maskFieldValue(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return "***";
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        
        if (lowerFieldName.contains("email")) {
            return "***@masked.com";
        } else if (lowerFieldName.contains("phone")) {
            return "***-***-****";
        } else if (lowerFieldName.contains("id")) {
            return "***" + value.substring(Math.max(0, value.length() - 3));
        } else if (lowerFieldName.contains("name")) {
            return "User " + value.hashCode() % 1000;
        } else {
            return "***";
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
     * Get PDF generator status
     */
    public String getPDFGeneratorStatus() {
        return String.format(
            "PDF Report Generator Status:\n" +
            "Service: Active\n" +
            "PDF Library: OpenPDF 1.3.30\n" +
            "Page Size: A4\n" +
            "Field Masking: Enabled\n" +
            "Role-Based Filtering: Enabled\n" +
            "Template Integration: Enabled"
        );
    }
    
    /**
     * Get default visible fields for a role (fallback when JWT is not available)
     */
    private List<String> getDefaultVisibleFields(String userRole) {
        UserRole role = UserRole.from(userRole);
        return switch (role) {
            case ADMIN, SYSTEM_SCHEDULER -> Arrays.asList("timesheetId", "providerName", "hours", "date", "county", "district");
            case SUPERVISOR -> Arrays.asList("timesheetId", "providerName", "hours", "date", "county");
            case CASE_WORKER -> Arrays.asList("timesheetId", "providerName", "hours", "date");
            case PROVIDER, RECIPIENT -> Arrays.asList("timesheetId", "providerName", "hours");
        };
    }
    
    /**
     * Extract start date from additional data
     */
    private LocalDate extractStartDate(Map<String, Object> additionalData) {
        if (additionalData == null) return null;
        
        // Try startDate field first
        Object startDateObj = additionalData.get("startDate");
        if (startDateObj instanceof LocalDate) {
            return (LocalDate) startDateObj;
        } else if (startDateObj instanceof String) {
            try {
                return LocalDate.parse((String) startDateObj);
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Try parsing from dateRange string
        String dateRange = (String) additionalData.getOrDefault("dateRange", "");
        if (dateRange.contains(" to ")) {
            try {
                String[] parts = dateRange.split(" to ");
                return LocalDate.parse(parts[0].trim());
            } catch (Exception e) {
                // Ignore
            }
        }
        
        return null;
    }
    
    /**
     * Extract end date from additional data
     */
    private LocalDate extractEndDate(Map<String, Object> additionalData) {
        if (additionalData == null) return null;
        
        // Try endDate field first
        Object endDateObj = additionalData.get("endDate");
        if (endDateObj instanceof LocalDate) {
            return (LocalDate) endDateObj;
        } else if (endDateObj instanceof String) {
            try {
                return LocalDate.parse((String) endDateObj);
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Try parsing from dateRange string
        String dateRange = (String) additionalData.getOrDefault("dateRange", "");
        if (dateRange.contains(" to ")) {
            try {
                String[] parts = dateRange.split(" to ");
                if (parts.length > 1) {
                    return LocalDate.parse(parts[1].trim());
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        return null;
    }
    
    /**
     * Add professional cover page
     */
    private void addCoverPage(Document document, String reportType, String userRole, Map<String, Object> additionalData) throws DocumentException {
        // Title with larger font
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.DARK_GRAY);
        Paragraph title = new Paragraph(getReportTitle(reportType), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(30);
        document.add(title);
        
        // Subtitle
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, new Color(100, 100, 100));
        Paragraph subtitle = new Paragraph("Comprehensive Analytics Report", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(40);
        document.add(subtitle);
        
        // Report metadata in a table
        PdfPTable metadataTable = new PdfPTable(2);
        metadataTable.setWidthPercentage(70);
        metadataTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        metadataTable.setSpacingBefore(30);
        metadataTable.setSpacingAfter(30);
        
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
        
        addMetadataRow(metadataTable, "Generated For:", userRole, labelFont, valueFont);
        addMetadataRow(metadataTable, "Report Type:", reportType, labelFont, valueFont);
        
        String dateRange = (String) additionalData.getOrDefault("dateRange", "All Time");
        addMetadataRow(metadataTable, "Date Range:", dateRange, labelFont, valueFont);
        
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        addMetadataRow(metadataTable, "Generated At:", generatedAt, labelFont, valueFont);
        
        document.add(metadataTable);
        
        // Add decorative line
        LineSeparator line = new LineSeparator(1f, 100f, Color.DARK_GRAY, Element.ALIGN_CENTER, -1);
        document.add(new Chunk(line));
    }
    
    /**
     * Helper to add metadata row
     */
    private void addMetadataRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) throws DocumentException {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }
    
    /**
     * Add executive summary section with KPIs and charts
     */
    private void addExecutiveSummary(Document document, List<Map<String, Object>> reportData, 
                                    String userRole, String jwtToken, LocalDate startDate, 
                                    LocalDate endDate, String county, String districtId) throws DocumentException {
        // Section title
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(50, 50, 150));
        Paragraph sectionTitle = new Paragraph("Executive Summary", sectionFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(20);
        document.add(sectionTitle);
        
        // Calculate KPIs from report data
        ExecutiveSummaryMetrics metrics = calculateExecutiveSummaryMetrics(reportData, startDate, endDate, county, districtId);
        
        // KPI Cards in a 2x2 grid
        PdfPTable kpiTable = new PdfPTable(2);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingBefore(10);
        kpiTable.setSpacingAfter(20);
        
        addKPICard(kpiTable, "Total Records", String.valueOf(metrics.totalRecords), new Color(70, 130, 180));
        addKPICard(kpiTable, "Total Hours", String.format("%.2f", metrics.totalHours), new Color(60, 179, 113));
        addKPICard(kpiTable, "Total Amount", String.format("$%.2f", metrics.totalAmount), new Color(255, 140, 0));
        addKPICard(kpiTable, "Approval Rate", String.format("%.1f%%", metrics.approvalRate), new Color(220, 20, 60));
        
        document.add(kpiTable);
        
        // Status Distribution Chart
        addStatusDistributionChart(document, metrics.statusDistribution);
        
        // Top 5 Providers
        addTopProvidersSection(document, metrics.topProviders);
    }
    
    /**
     * Calculate executive summary metrics
     */
    private ExecutiveSummaryMetrics calculateExecutiveSummaryMetrics(List<Map<String, Object>> reportData, 
                                                                     LocalDate startDate, LocalDate endDate,
                                                                     String county, String districtId) {
        ExecutiveSummaryMetrics metrics = new ExecutiveSummaryMetrics();
        
        if (reportData == null || reportData.isEmpty()) {
            return metrics;
        }
        
        metrics.totalRecords = reportData.size();
        
        Map<String, Integer> statusCounts = new HashMap<>();
        Map<String, Double> providerHours = new HashMap<>();
        Map<String, Double> providerAmounts = new HashMap<>();
        
        for (Map<String, Object> record : reportData) {
            // Total hours
            Object hoursObj = record.get("totalHours");
            if (hoursObj != null) {
                try {
                    double hours = hoursObj instanceof Number ? ((Number) hoursObj).doubleValue() : Double.parseDouble(hoursObj.toString());
                    metrics.totalHours += hours;
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            // Total amount
            Object amountObj = record.get("totalAmount");
            if (amountObj != null) {
                try {
                    double amount = amountObj instanceof Number ? ((Number) amountObj).doubleValue() : Double.parseDouble(amountObj.toString());
                    metrics.totalAmount += amount;
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            // Status distribution
            String status = record.get("status") != null ? record.get("status").toString() : "UNKNOWN";
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            
            // Provider performance
            String providerName = record.get("providerName") != null ? record.get("providerName").toString() : "Unknown";
            if (hoursObj != null) {
                try {
                    double hours = hoursObj instanceof Number ? ((Number) hoursObj).doubleValue() : Double.parseDouble(hoursObj.toString());
                    providerHours.put(providerName, providerHours.getOrDefault(providerName, 0.0) + hours);
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (amountObj != null) {
                try {
                    double amount = amountObj instanceof Number ? ((Number) amountObj).doubleValue() : Double.parseDouble(amountObj.toString());
                    providerAmounts.put(providerName, providerAmounts.getOrDefault(providerName, 0.0) + amount);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        metrics.statusDistribution = statusCounts;
        
        // Calculate approval rate
        int approvedCount = statusCounts.getOrDefault("APPROVED", 0);
        metrics.approvalRate = metrics.totalRecords > 0 ? (approvedCount * 100.0 / metrics.totalRecords) : 0.0;
        
        // Top 5 providers by hours
        metrics.topProviders = providerHours.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .map(e -> new ProviderPerformance(e.getKey(), e.getValue(), providerAmounts.getOrDefault(e.getKey(), 0.0)))
            .collect(Collectors.toList());
        
        return metrics;
    }
    
    /**
     * Add KPI card to table
     */
    private void addKPICard(PdfPTable table, String label, String value, Color bgColor) throws DocumentException {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(15);
        cell.setBorder(10);
        cell.setBorderColor(Color.WHITE);
        
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.WHITE);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
        
        Paragraph content = new Paragraph();
        content.add(new Chunk(label + "\n", labelFont));
        content.add(new Chunk(value, valueFont));
        content.setAlignment(Element.ALIGN_CENTER);
        
        cell.addElement(content);
        table.addCell(cell);
    }
    
    /**
     * Add status distribution chart
     */
    private void addStatusDistributionChart(Document document, Map<String, Integer> statusDistribution) throws DocumentException {
        if (statusDistribution == null || statusDistribution.isEmpty()) {
            return;
        }
        
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Paragraph chartTitle = new Paragraph("Status Distribution", sectionFont);
        chartTitle.setSpacingBefore(20);
        chartTitle.setSpacingAfter(10);
        document.add(chartTitle);
        
        // Create a simple bar chart representation using a table
        PdfPTable chartTable = new PdfPTable(2);
        chartTable.setWidthPercentage(80);
        chartTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        chartTable.setSpacingBefore(10);
        chartTable.setSpacingAfter(20);
        
        int maxCount = statusDistribution.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        
        for (Map.Entry<String, Integer> entry : statusDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList())) {
            
            String status = entry.getKey();
            int count = entry.getValue();
            double percentage = maxCount > 0 ? (count * 100.0 / maxCount) : 0;
            
            // Status label
            PdfPCell labelCell = new PdfPCell(new Phrase(status, labelFont));
            labelCell.setBorder(PdfPCell.NO_BORDER);
            labelCell.setPadding(5);
            chartTable.addCell(labelCell);
            
            // Bar representation
            PdfPCell barCell = new PdfPCell();
            barCell.setBorder(PdfPCell.NO_BORDER);
            barCell.setPadding(5);
            
            // Create a colored bar
            float barWidth = (float) (percentage * 3); // Scale for visual representation
            if (barWidth > 0) {
                Color barColor = getStatusColor(status);
                com.lowagie.text.Rectangle bar = new com.lowagie.text.Rectangle(0, 0, barWidth, 10);
                bar.setBackgroundColor(barColor);
                bar.setBorder(com.lowagie.text.Rectangle.BOX);
                bar.setBorderColor(barColor);
                
                Paragraph barPara = new Paragraph();
                barPara.add(new Chunk(" ", FontFactory.getFont(FontFactory.HELVETICA, 1)));
                barPara.add(new Chunk(String.format("%d (%.1f%%)", count, percentage), valueFont));
                barCell.addElement(barPara);
            }
            
            chartTable.addCell(barCell);
        }
        
        document.add(chartTable);
    }
    
    /**
     * Get color for status
     */
    private Color getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "APPROVED": return new Color(60, 179, 113);
            case "PENDING_REVIEW": case "SUBMITTED": return new Color(255, 165, 0);
            case "REJECTED": return new Color(220, 20, 60);
            case "REVISION_REQUIRED": return new Color(255, 140, 0);
            default: return new Color(128, 128, 128);
        }
    }
    
    /**
     * Add top providers section
     */
    private void addTopProvidersSection(Document document, List<ProviderPerformance> topProviders) throws DocumentException {
        if (topProviders == null || topProviders.isEmpty()) {
            return;
        }
        
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Paragraph sectionTitle = new Paragraph("Top 5 Providers by Hours", sectionFont);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        
        // Header
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        addTableHeader(table, "Rank", headerFont);
        addTableHeader(table, "Provider Name", headerFont);
        addTableHeader(table, "Total Hours", headerFont);
        addTableHeader(table, "Total Amount", headerFont);
        
        // Data rows
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        int rank = 1;
        for (ProviderPerformance provider : topProviders) {
            addTableCell(table, String.valueOf(rank++), dataFont, false);
            addTableCell(table, provider.name, dataFont, false);
            addTableCell(table, String.format("%.2f", provider.hours), dataFont, true);
            addTableCell(table, String.format("$%.2f", provider.amount), dataFont, true);
        }
        
        document.add(table);
    }
    
    /**
     * Add analytics section with trends and demographic breakdowns
     */
    private void addAnalyticsSection(Document document, List<Map<String, Object>> reportData,
                                    String userRole, String jwtToken, LocalDate startDate,
                                    LocalDate endDate, String county, String districtId) throws DocumentException {
        // Section title
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(50, 50, 150));
        Paragraph sectionTitle = new Paragraph("Analytics & Insights", sectionFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(20);
        document.add(sectionTitle);
        
        // Demographic Breakdowns
        addDemographicBreakdowns(document, reportData);
        
        // Geographic Analysis
        addGeographicAnalysis(document, reportData);
        
        // Service Type Analysis
        addServiceTypeAnalysis(document, reportData);
    }
    
    /**
     * Add demographic breakdowns
     */
    private void addDemographicBreakdowns(Document document, List<Map<String, Object>> reportData) throws DocumentException {
        Font subsectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Paragraph subsectionTitle = new Paragraph("Demographic Breakdowns", subsectionFont);
        subsectionTitle.setSpacingBefore(20);
        subsectionTitle.setSpacingAfter(10);
        document.add(subsectionTitle);
        
        // Gender distribution
        Map<String, Integer> genderDistribution = new HashMap<>();
        Map<String, Integer> ethnicityDistribution = new HashMap<>();
        
        for (Map<String, Object> record : reportData) {
            String providerGender = record.get("providerGender") != null ? record.get("providerGender").toString() : null;
            String recipientGender = record.get("recipientGender") != null ? record.get("recipientGender").toString() : null;
            
            if (providerGender != null) {
                genderDistribution.put(providerGender, genderDistribution.getOrDefault(providerGender, 0) + 1);
            }
            if (recipientGender != null) {
                genderDistribution.put(recipientGender, genderDistribution.getOrDefault(recipientGender, 0) + 1);
            }
            
            String providerEthnicity = record.get("providerEthnicity") != null ? record.get("providerEthnicity").toString() : null;
            String recipientEthnicity = record.get("recipientEthnicity") != null ? record.get("recipientEthnicity").toString() : null;
            
            if (providerEthnicity != null) {
                ethnicityDistribution.put(providerEthnicity, ethnicityDistribution.getOrDefault(providerEthnicity, 0) + 1);
            }
            if (recipientEthnicity != null) {
                ethnicityDistribution.put(recipientEthnicity, ethnicityDistribution.getOrDefault(recipientEthnicity, 0) + 1);
            }
        }
        
        if (!genderDistribution.isEmpty()) {
            addDistributionTable(document, "Gender Distribution", genderDistribution);
        }
        
        if (!ethnicityDistribution.isEmpty()) {
            addDistributionTable(document, "Ethnicity Distribution", ethnicityDistribution);
        }
    }
    
    /**
     * Add distribution table
     */
    private void addDistributionTable(Document document, String title, Map<String, Integer> distribution) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Paragraph tableTitle = new Paragraph(title, titleFont);
        tableTitle.setSpacingBefore(10);
        tableTitle.setSpacingAfter(5);
        document.add(tableTitle);
        
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingBefore(5);
        table.setSpacingAfter(15);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        
        addTableHeader(table, "Category", headerFont);
        addTableHeader(table, "Count", headerFont);
        addTableHeader(table, "Percentage", headerFont);
        
        int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<String, Integer> entry : distribution.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList())) {
            addTableCell(table, entry.getKey(), dataFont, false);
            addTableCell(table, String.valueOf(entry.getValue()), dataFont, true);
            double percentage = total > 0 ? (entry.getValue() * 100.0 / total) : 0;
            addTableCell(table, String.format("%.1f%%", percentage), dataFont, true);
        }
        
        document.add(table);
    }
    
    /**
     * Add geographic analysis
     */
    private void addGeographicAnalysis(Document document, List<Map<String, Object>> reportData) throws DocumentException {
        Font subsectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Paragraph subsectionTitle = new Paragraph("Geographic Analysis", subsectionFont);
        subsectionTitle.setSpacingBefore(20);
        subsectionTitle.setSpacingAfter(10);
        document.add(subsectionTitle);
        
        Map<String, Integer> countyDistribution = new HashMap<>();
        Map<String, Integer> districtDistribution = new HashMap<>();
        
        for (Map<String, Object> record : reportData) {
            String county = record.get("providerCounty") != null ? record.get("providerCounty").toString() : 
                           record.get("projectCounty") != null ? record.get("projectCounty").toString() : null;
            if (county != null) {
                countyDistribution.put(county, countyDistribution.getOrDefault(county, 0) + 1);
            }
            
            String district = record.get("districtName") != null ? record.get("districtName").toString() : null;
            if (district != null) {
                districtDistribution.put(district, districtDistribution.getOrDefault(district, 0) + 1);
            }
        }
        
        if (!countyDistribution.isEmpty()) {
            addDistributionTable(document, "County Distribution", countyDistribution);
        }
        
        if (!districtDistribution.isEmpty()) {
            addDistributionTable(document, "District Distribution", districtDistribution);
        }
    }
    
    /**
     * Add service type analysis
     */
    private void addServiceTypeAnalysis(Document document, List<Map<String, Object>> reportData) throws DocumentException {
        Font subsectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Paragraph subsectionTitle = new Paragraph("Service Type Analysis", subsectionFont);
        subsectionTitle.setSpacingBefore(20);
        subsectionTitle.setSpacingAfter(10);
        document.add(subsectionTitle);
        
        Map<String, Integer> serviceTypeDistribution = new HashMap<>();
        Map<String, Double> serviceTypeHours = new HashMap<>();
        Map<String, Double> serviceTypeAmounts = new HashMap<>();
        
        for (Map<String, Object> record : reportData) {
            String serviceType = record.get("serviceType") != null ? record.get("serviceType").toString() : "Unknown";
            serviceTypeDistribution.put(serviceType, serviceTypeDistribution.getOrDefault(serviceType, 0) + 1);
            
            Object hoursObj = record.get("totalHours");
            if (hoursObj != null) {
                try {
                    double hours = hoursObj instanceof Number ? ((Number) hoursObj).doubleValue() : Double.parseDouble(hoursObj.toString());
                    serviceTypeHours.put(serviceType, serviceTypeHours.getOrDefault(serviceType, 0.0) + hours);
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            Object amountObj = record.get("totalAmount");
            if (amountObj != null) {
                try {
                    double amount = amountObj instanceof Number ? ((Number) amountObj).doubleValue() : Double.parseDouble(amountObj.toString());
                    serviceTypeAmounts.put(serviceType, serviceTypeAmounts.getOrDefault(serviceType, 0.0) + amount);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        if (!serviceTypeDistribution.isEmpty()) {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(20);
            
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            
            addTableHeader(table, "Service Type", headerFont);
            addTableHeader(table, "Count", headerFont);
            addTableHeader(table, "Total Hours", headerFont);
            addTableHeader(table, "Total Amount", headerFont);
            
            for (Map.Entry<String, Integer> entry : serviceTypeDistribution.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList())) {
                String serviceType = entry.getKey();
                addTableCell(table, serviceType, dataFont, false);
                addTableCell(table, String.valueOf(entry.getValue()), dataFont, true);
                addTableCell(table, String.format("%.2f", serviceTypeHours.getOrDefault(serviceType, 0.0)), dataFont, true);
                addTableCell(table, String.format("$%.2f", serviceTypeAmounts.getOrDefault(serviceType, 0.0)), dataFont, true);
            }
            
            document.add(table);
        }
    }
    
    /**
     * Add detailed data section with enhanced table formatting
     */
    private void addDetailedDataSection(Document document, List<Map<String, Object>> reportData,
                                       List<String> visibleFields, String userRole) throws DocumentException {
        // Section title
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(50, 50, 150));
        Paragraph sectionTitle = new Paragraph("Detailed Data", sectionFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(20);
        document.add(sectionTitle);
        
        // Use enhanced data table
        addEnhancedDataTable(document, reportData, visibleFields, userRole);
    }
    
    /**
     * Add enhanced data table with better formatting
     */
    private void addEnhancedDataTable(Document document, List<Map<String, Object>> reportData,
                                     List<String> visibleFields, String userRole) throws DocumentException {
        if (reportData == null || reportData.isEmpty()) {
            Paragraph noData = new Paragraph("No data available for this report.", 
                FontFactory.getFont(FontFactory.HELVETICA, 10));
            document.add(noData);
            return;
        }
        
        // Create table with visible fields
        PdfPTable table = new PdfPTable(visibleFields.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        // Set column widths - adjust based on number of columns
        float[] columnWidths = new float[visibleFields.size()];
        float widthPerColumn = 100.0f / visibleFields.size();
        for (int i = 0; i < visibleFields.size(); i++) {
            columnWidths[i] = widthPerColumn;
        }
        table.setWidths(columnWidths);
        
        // Add header row with better styling
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        
        for (String field : visibleFields) {
            PdfPCell headerCell = new PdfPCell(new Phrase(formatFieldName(field), headerFont));
            headerCell.setBackgroundColor(new Color(50, 50, 150));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(8);
            headerCell.setBorderWidth(1);
            table.addCell(headerCell);
        }
        
        // Add data rows with alternating colors
        boolean alternate = false;
        Color rowColor1 = new Color(245, 245, 245);
        Color rowColor2 = Color.WHITE;
        
        for (Map<String, Object> record : reportData) {
            for (String field : visibleFields) {
                Object value = record.get(field);
                String cellValue = (value != null) ? value.toString() : "";
                
                // Apply field masking based on role
                if (shouldMaskField(field, userRole)) {
                    cellValue = maskFieldValue(field, cellValue);
                }
                
                PdfPCell cell = new PdfPCell(new Phrase(cellValue, cellFont));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(alternate ? rowColor1 : rowColor2);
                cell.setBorderWidth(0.5f);
                table.addCell(cell);
            }
            alternate = !alternate;
        }
        
        document.add(table);
    }
    
    /**
     * Format field name for display
     */
    private String formatFieldName(String fieldName) {
        return fieldName.replaceAll("([A-Z])", " $1").trim();
    }
    
    /**
     * Add table header cell
     */
    private void addTableHeader(PdfPTable table, String text, Font font) throws DocumentException {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(50, 50, 150));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        cell.setBorderWidth(1);
        table.addCell(cell);
    }
    
    /**
     * Add table data cell
     */
    private void addTableCell(PdfPTable table, String text, Font font, boolean alignRight) throws DocumentException {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignRight ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }
    
    /**
     * Add appendices section
     */
    private void addAppendices(Document document, List<Map<String, Object>> reportData,
                              String userRole, String jwtToken, Map<String, Object> additionalData) throws DocumentException {
        document.newPage();
        
        // Section title
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(50, 50, 150));
        Paragraph sectionTitle = new Paragraph("Appendices", sectionFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(20);
        document.add(sectionTitle);
        
        Font subsectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        
        // Data Source Information
        Paragraph dataSourceTitle = new Paragraph("Data Source Information", subsectionFont);
        dataSourceTitle.setSpacingBefore(15);
        dataSourceTitle.setSpacingAfter(10);
        document.add(dataSourceTitle);
        
        Paragraph dataSource = new Paragraph("Source: Timesheet Management System Database", dataFont);
        dataSource.setSpacingAfter(5);
        document.add(dataSource);
        
        String dateRange = (String) additionalData.getOrDefault("dateRange", "All Time");
        Paragraph dateRangePara = new Paragraph("Date Range: " + dateRange, dataFont);
        dateRangePara.setSpacingAfter(5);
        document.add(dateRangePara);
        
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Paragraph generatedPara = new Paragraph("Generated At: " + generatedAt, dataFont);
        generatedPara.setSpacingAfter(15);
        document.add(generatedPara);
        
        // Field Visibility Information
        Paragraph fieldVisibilityTitle = new Paragraph("Field Visibility", subsectionFont);
        fieldVisibilityTitle.setSpacingBefore(15);
        fieldVisibilityTitle.setSpacingAfter(10);
        document.add(fieldVisibilityTitle);
        
        List<String> visibleFields = fieldVisibilityService.getVisibleFields(userRole, jwtToken);
        Paragraph fieldInfo = new Paragraph("Visible Fields (" + visibleFields.size() + "): " + 
            String.join(", ", visibleFields), dataFont);
        fieldInfo.setSpacingAfter(5);
        document.add(fieldInfo);
        
        Paragraph roleInfo = new Paragraph("User Role: " + userRole, dataFont);
        roleInfo.setSpacingAfter(15);
        document.add(roleInfo);
        
        // Applied Filters
        Paragraph filtersTitle = new Paragraph("Applied Filters", subsectionFont);
        filtersTitle.setSpacingBefore(15);
        filtersTitle.setSpacingAfter(10);
        document.add(filtersTitle);
        
        String county = (String) additionalData.getOrDefault("userCounty", null);
        String districtId = (String) additionalData.getOrDefault("districtId", null);
        
        if (county != null) {
            Paragraph countyFilter = new Paragraph("County: " + county, dataFont);
            countyFilter.setSpacingAfter(5);
            document.add(countyFilter);
        }
        
        if (districtId != null) {
            Paragraph districtFilter = new Paragraph("District: " + districtId, dataFont);
            districtFilter.setSpacingAfter(5);
            document.add(districtFilter);
        }
        
        if (county == null && districtId == null) {
            Paragraph noFilters = new Paragraph("No geographic filters applied", dataFont);
            noFilters.setSpacingAfter(5);
            document.add(noFilters);
        }
    }
    
    /**
     * Inner class for executive summary metrics
     */
    private static class ExecutiveSummaryMetrics {
        int totalRecords = 0;
        double totalHours = 0.0;
        double totalAmount = 0.0;
        double approvalRate = 0.0;
        Map<String, Integer> statusDistribution = new HashMap<>();
        List<ProviderPerformance> topProviders = new ArrayList<>();
    }
    
    /**
     * Inner class for provider performance
     */
    private static class ProviderPerformance {
        String name;
        double hours;
        double amount;
        
        ProviderPerformance(String name, double hours, double amount) {
            this.name = name;
            this.hours = hours;
            this.amount = amount;
        }
    }
}
