package com.cmips.service;

import com.cmips.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EmailReportService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Autowired
    private ReportTemplateService reportTemplateService;
    
    @Autowired
    private PDFReportGeneratorService pdfReportGeneratorService;
    
    @Value("${report.email.from:reports@system.com}")
    private String fromEmail;
    
    @Value("${report.email.subject-prefix:[Report System]}")
    private String subjectPrefix;
    
    public EmailReportService() {
        System.out.println("🔧 EmailReportService: Initializing email report service");
    }
    
    /**
     * Send report via email with PDF attachment
     */
    public boolean sendReportEmail(String reportType, String userRole, 
                                  List<Map<String, Object>> reportData, 
                                  Map<String, Object> additionalData,
                                  List<String> recipients) {
        System.out.println("🔧 EmailReportService: sendReportEmail called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use sendReportEmail(reportType, userRole, reportData, additionalData, recipients, jwtToken) with JWT token.");
    }
    
    public boolean sendReportEmail(String reportType, String userRole,
                                  List<Map<String, Object>> reportData,
                                  Map<String, Object> additionalData,
                                  List<String> recipients, String jwtToken) {
        try {
            if (mailSender == null) {
                System.out.println("⚠️ EmailReportService: Mail sender not configured - email delivery disabled");
                return false;
            }

            UserRole role = UserRole.from(userRole);
            String canonicalRole = role.name();
            System.out.println("📧 EmailReportService: Sending report email for type: " + reportType + ", role: " + canonicalRole);
            System.out.println("📧 Recipients: " + recipients);

            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token is required for email report generation. No fallback methods available.");
            }
            
            // Generate PDF report with JWT token for proper field masking
            byte[] pdfBytes = pdfReportGeneratorService.generatePDFReport(reportType, canonicalRole, reportData, additionalData, jwtToken);
            System.out.println("📄 PDF generated: " + pdfBytes.length + " bytes");
            
            // Generate email content
            String emailContent = reportTemplateService.renderEmailTemplate(reportType, canonicalRole, reportData, additionalData);
            System.out.println("📧 Email content generated: " + emailContent.length() + " chars");
            
            // Create email message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set email properties
            helper.setFrom(fromEmail);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(generateEmailSubject(reportType, canonicalRole, additionalData));
            helper.setText(emailContent, true); // HTML content
            
            // Attach PDF
            String attachmentName = generateAttachmentName(reportType, canonicalRole, additionalData);
            helper.addAttachment(attachmentName, new org.springframework.core.io.ByteArrayResource(pdfBytes));
            
            // Send email
            mailSender.send(message);
            
            System.out.println("✅ EmailReportService: Report email sent successfully to " + recipients.size() + " recipients");
            return true;
            
        } catch (MessagingException e) {
            System.err.println("❌ Error sending report email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending report email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send report email to single recipient
     */
    public boolean sendReportEmail(String reportType, String userRole, 
                                  List<Map<String, Object>> reportData, 
                                  Map<String, Object> additionalData,
                                  String recipient) {
        return sendReportEmail(reportType, userRole, reportData, additionalData, List.of(recipient));
    }
    
    
    /**
     * Send scheduled report email
     */
    public boolean sendScheduledReportEmail(String reportType, String userRole, 
                                           List<Map<String, Object>> reportData, 
                                           Map<String, Object> additionalData) {
        System.out.println("🔧 EmailReportService: sendScheduledReportEmail called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use sendScheduledReportEmail(reportType, userRole, reportData, additionalData, jwtToken) with JWT token.");
    }
    
    public boolean sendScheduledReportEmail(String reportType, String userRole, 
                                           List<Map<String, Object>> reportData, 
                                           Map<String, Object> additionalData, String jwtToken) {
        try {
            UserRole role = UserRole.from(userRole);
            String canonicalRole = role.name();
            System.out.println("📧 EmailReportService: Sending scheduled report email for type: " + reportType);
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token is required for scheduled email report generation. No fallback methods available.");
            }
            
            // Get default recipients for scheduled reports
            List<String> recipients = getScheduledReportRecipients(role);
            System.out.println("📧 Scheduled report recipients: " + recipients);
            
            // Add scheduled report metadata
            additionalData.put("isScheduled", true);
            additionalData.put("scheduledAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return sendReportEmail(reportType, canonicalRole, reportData, additionalData, recipients, jwtToken);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending scheduled report email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate email subject
     */
    private String generateEmailSubject(String reportType, String userRole, Map<String, Object> additionalData) {
        String dateRange = (String) additionalData.getOrDefault("dateRange", "N/A");
        String timestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        return String.format("%s %s - %s (%s) - %s", 
                           subjectPrefix, 
                           getReportTitle(reportType), 
                           userRole, 
                           dateRange, 
                           timestamp);
    }
    
    /**
     * Generate attachment filename
     */
    private String generateAttachmentName(String reportType, String userRole, Map<String, Object> additionalData) {
        String timestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String dateRange = (String) additionalData.getOrDefault("dateRange", "N/A");
        String cleanDateRange = dateRange.replace(" to ", "_").replace("-", "");
        
        return String.format("%s_%s_%s_%s.pdf", 
                           reportType.toLowerCase(), 
                           userRole.toLowerCase(), 
                           cleanDateRange, 
                           timestamp);
    }
    
    /**
     * Get recipients for scheduled reports based on user role
     */
    private List<String> getScheduledReportRecipients(UserRole role) {
        return switch (role) {
            case ADMIN, SYSTEM_SCHEDULER -> List.of("admin@system.com", "central@system.com");
            case SUPERVISOR -> List.of("district-leads@system.com");
            case CASE_WORKER -> List.of("county-ops@system.com");
            case PROVIDER, RECIPIENT -> List.of("notifications@system.com");
        };
    }
    
    /**
     * Get report title based on report type
     */
    private String getReportTitle(String reportType) {
        // Only timesheet reports for now
        return "Timesheet Report";
    }
    
    
    /**
     * Get email service status
     */
    public String getEmailServiceStatus() {
        return String.format(
            "Email Report Service Status:\n" +
            "Service: Active\n" +
            "From Email: %s\n" +
            "Subject Prefix: %s\n" +
            "PDF Attachments: Enabled\n" +
            "HTML Content: Enabled\n" +
            "Template Integration: Enabled\n" +
            "Scheduled Reports: Enabled",
            fromEmail,
            subjectPrefix
        );
    }
}
