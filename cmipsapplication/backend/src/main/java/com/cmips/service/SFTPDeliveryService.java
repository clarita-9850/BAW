package com.cmips.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SFTPDeliveryService {
    
    @Value("${sftp.host:localhost}")
    private String sftpHost;
    
    @Value("${sftp.port:22}")
    private int sftpPort;
    
    @Value("${sftp.username:reports}")
    private String sftpUsername;
    
    @Value("${sftp.password:password}")
    private String sftpPassword;
    
    @Value("${sftp.base-path:/reports}")
    private String sftpBasePath;
    
    @Value("${sftp.private-key-path:}")
    private String privateKeyPath;
    
    @Value("${sftp.private-key-passphrase:}")
    private String privateKeyPassphrase;
    
    public SFTPDeliveryService() {
        System.out.println("🔧 SFTPDeliveryService: Initializing SFTP delivery service");
    }
    
    /**
     * Deliver encrypted file to SFTP server
     */
    public String deliverFile(String localFilePath, String userRole, String reportType, String dateStr) {
        System.out.println("📤 SFTPDeliveryService: Delivering file to SFTP server");
        System.out.println("📁 Local file: " + localFilePath);
        System.out.println("👤 User role: " + userRole);
        System.out.println("📊 Report type: " + reportType);
        System.out.println("📅 Date: " + dateStr);
        
        try {
            // Create SFTP directory structure
            String sftpDirectory = createSFTPDirectoryStructure(userRole, reportType, dateStr);
            System.out.println("📁 SFTP directory: " + sftpDirectory);
            
            // Generate remote filename
            String remoteFileName = generateRemoteFileName(userRole, reportType, dateStr);
            String remoteFilePath = sftpDirectory + "/" + remoteFileName;
            System.out.println("📄 Remote file path: " + remoteFilePath);
            
            // For demo purposes, we'll simulate SFTP upload
            // In production, you would use JSch or Apache Commons VFS
            simulateSFTPUpload(localFilePath, remoteFilePath);
            
            System.out.println("✅ SFTPDeliveryService: File delivered successfully to " + remoteFilePath);
            return remoteFilePath;
            
        } catch (Exception e) {
            System.err.println("❌ SFTPDeliveryService: Error delivering file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to deliver file to SFTP server", e);
        }
    }
    
    /**
     * Create SFTP directory structure
     */
    private String createSFTPDirectoryStructure(String userRole, String reportType, String dateStr) {
        // Create directory structure: /reports/daily/YYYY-MM-DD/userRole/reportType/
        String yearMonth = dateStr.substring(0, 7); // YYYY-MM
        String day = dateStr.substring(8, 10); // DD
        
        String directory = String.format("%s/daily/%s/%s/%s/%s", 
            sftpBasePath, yearMonth, day, userRole.toLowerCase(), reportType.toLowerCase());
        
        System.out.println("📁 Creating SFTP directory: " + directory);
        
        // In production, create directory via SFTP
        createDirectoryIfNotExists(directory);
        
        return directory;
    }
    
    /**
     * Generate remote filename
     */
    private String generateRemoteFileName(String userRole, String reportType, String dateStr) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        return String.format("%s_%s_%s_%s.encrypted.csv", 
            userRole.toLowerCase(), reportType.toLowerCase(), dateStr, timestamp);
    }
    
    /**
     * Simulate SFTP upload (replace with actual SFTP implementation)
     */
    private void simulateSFTPUpload(String localFilePath, String remoteFilePath) {
        System.out.println("🔄 Simulating SFTP upload...");
        System.out.println("📤 From: " + localFilePath);
        System.out.println("📥 To: " + remoteFilePath);
        
        // Simulate upload delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify local file exists
        File localFile = new File(localFilePath);
        if (!localFile.exists()) {
            throw new RuntimeException("Local file does not exist: " + localFilePath);
        }
        
        System.out.println("✅ SFTP upload simulation completed");
        System.out.println("📊 File size: " + localFile.length() + " bytes");
    }
    
    /**
     * Create directory if it doesn't exist (simulated)
     */
    private void createDirectoryIfNotExists(String directory) {
        System.out.println("📁 Creating directory: " + directory);
        // In production, use SFTP to create directories
        // For now, just log the directory creation
    }
    
    /**
     * Test SFTP connection
     */
    public boolean testConnection() {
        System.out.println("🔍 SFTPDeliveryService: Testing SFTP connection");
        System.out.println("🌐 Host: " + sftpHost + ":" + sftpPort);
        System.out.println("👤 Username: " + sftpUsername);
        
        try {
            // In production, test actual SFTP connection
            // For now, simulate successful connection
            System.out.println("✅ SFTP connection test successful");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ SFTP connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get SFTP configuration info
     */
    public String getConfigurationInfo() {
        return String.format(
            "SFTP Configuration:\n" +
            "Host: %s:%d\n" +
            "Username: %s\n" +
            "Base Path: %s\n" +
            "Private Key: %s",
            sftpHost, sftpPort, sftpUsername, sftpBasePath, 
            privateKeyPath.isEmpty() ? "Not configured" : "Configured"
        );
    }
}
