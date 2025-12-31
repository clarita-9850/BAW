package com.example.kafkaeventdrivenapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@ConfigurationProperties(prefix = "notification.email")
public class NotificationService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    private boolean enabled = true;
    private String from = "reports@system.com";
    private String to = "mythreya9944@gmail.com";
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    private List<String> notificationLog = new ArrayList<>();
    
    public NotificationService() {
        System.out.println("🔧 NotificationService: Initializing notification service");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("🔧 NotificationService: PostConstruct - Initializing values");
        System.out.println("📧 Email enabled: " + enabled);
        System.out.println("🔧 From Email: " + from);
        System.out.println("🔧 To Email: " + to);
    }
    
    /**
     * Send delivery notification when report is successfully delivered
     */
    public void sendDeliveryNotification(String userRole, String reportType, String sftpPath, String message) {
        String subject = "Daily Report Delivered Successfully";
        String body = buildDeliveryNotificationBody(userRole, reportType, sftpPath, message);
        
        System.out.println("📧 NotificationService: Sending delivery notification");
        System.out.println("👤 User Role: " + userRole);
        System.out.println("📊 Report Type: " + reportType);
        System.out.println("📁 SFTP Path: " + sftpPath);
        
        try {
            // Send email notification
            if (enabled) {
                sendEmailNotification(subject, body);
            }
            
            // Log notification
            logNotification("DELIVERY_SUCCESS", userRole, reportType, message);
            
            System.out.println("✅ Delivery notification sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending delivery notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send error notification when report generation or delivery fails
     */
    public void sendErrorNotification(String subject, String errorMessage) {
        String body = buildErrorNotificationBody(errorMessage);
        
        System.out.println("❌ NotificationService: Sending error notification");
        System.out.println("📧 Subject: " + subject);
        System.out.println("💬 Message: " + errorMessage);
        
        try {
            // Send email notification
            if (enabled) {
                sendEmailNotification(subject, body);
            }
            
            // Log notification
            logNotification("ERROR", "SYSTEM", "ERROR", errorMessage);
            
            System.out.println("✅ Error notification sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending error notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send batch completion notification
     */
    public void sendBatchCompletionNotification(int totalReports, int successfulReports, int failedReports) {
        String subject = "Daily Report Batch Processing Completed";
        String body = buildBatchCompletionBody(totalReports, successfulReports, failedReports);
        
        System.out.println("📊 NotificationService: Sending batch completion notification");
        System.out.println("📈 Total Reports: " + totalReports);
        System.out.println("✅ Successful: " + successfulReports);
        System.out.println("❌ Failed: " + failedReports);
        
        try {
            // Send email notification
            if (enabled) {
                sendEmailNotification(subject, body);
            }
            
            // Log notification
            logNotification("BATCH_COMPLETED", "SYSTEM", "BATCH", 
                String.format("Total: %d, Success: %d, Failed: %d", totalReports, successfulReports, failedReports));
            
            System.out.println("✅ Batch completion notification sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending batch completion notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send email notification using JavaMailSender
     */
    private void sendEmailNotification(String subject, String body) {
        if (!enabled) {
            System.out.println("📧 Email notifications are disabled");
            return;
        }
        
        try {
            System.out.println("📧 Sending email notification...");
            System.out.println("📤 From: " + from);
            System.out.println("📥 To: " + to);
            System.out.println("📋 Subject: " + subject);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML content
            
            mailSender.send(message);
            System.out.println("✅ Email notification sent successfully");
            
        } catch (MessagingException e) {
            System.err.println("❌ Error sending email notification: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending email notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    /**
     * Build delivery notification body
     */
    private String buildDeliveryNotificationBody(String userRole, String reportType, String sftpPath, String message) {
        return String.format(
            "Daily Report Delivery Notification\n\n" +
            "Report Details:\n" +
            "- User Role: %s\n" +
            "- Report Type: %s\n" +
            "- Delivery Time: %s\n" +
            "- SFTP Path: %s\n" +
            "- Status: %s\n\n" +
            "The encrypted CSV report has been successfully delivered to the SFTP server.\n" +
            "You can access the report using the provided SFTP path.\n\n" +
            "Best regards,\n" +
            "Automated Report System",
            userRole,
            reportType,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            sftpPath,
            message
        );
    }
    
    /**
     * Build error notification body
     */
    private String buildErrorNotificationBody(String errorMessage) {
        return String.format(
            "Daily Report Generation Error\n\n" +
            "Error Details:\n" +
            "- Time: %s\n" +
            "- Error: %s\n\n" +
            "Please check the system logs for more details and take appropriate action.\n\n" +
            "Best regards,\n" +
            "Automated Report System",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            errorMessage
        );
    }
    
    /**
     * Build batch completion body
     */
    private String buildBatchCompletionBody(int totalReports, int successfulReports, int failedReports) {
        return String.format(
            "Daily Report Batch Processing Completed\n\n" +
            "Processing Summary:\n" +
            "- Total Reports: %d\n" +
            "- Successful: %d\n" +
            "- Failed: %d\n" +
            "- Success Rate: %.1f%%\n" +
            "- Completion Time: %s\n\n" +
            "All successful reports have been delivered to the SFTP server.\n" +
            "Please check the system logs for any failed reports.\n\n" +
            "Best regards,\n" +
            "Automated Report System",
            totalReports,
            successfulReports,
            failedReports,
            (double) successfulReports / totalReports * 100,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * Log notification
     */
    private void logNotification(String type, String userRole, String reportType, String message) {
        String logEntry = String.format("[%s] %s - %s/%s: %s",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            type,
            userRole,
            reportType,
            message
        );
        
        notificationLog.add(logEntry);
        System.out.println("📝 Notification logged: " + logEntry);
    }
    
    /**
     * Get notification log
     */
    public List<String> getNotificationLog() {
        return new ArrayList<>(notificationLog);
    }
    
    /**
     * Clear notification log
     */
    public void clearNotificationLog() {
        notificationLog.clear();
        System.out.println("🗑️ Notification log cleared");
    }
    
    
    /**
     * Get notification configuration
     */
    public String getNotificationConfiguration() {
        return String.format(
            "Notification Service Configuration:\n" +
            "Email Enabled: %s\n" +
            "From Email: %s\n" +
            "To Email: %s\n" +
            "Total Notifications Sent: %d",
            enabled,
            from,
            to,
            notificationLog.size()
        );
    }
}
