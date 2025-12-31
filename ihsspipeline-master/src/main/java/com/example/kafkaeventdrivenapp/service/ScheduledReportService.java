package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.BIReportRequest;
import com.example.kafkaeventdrivenapp.model.ReportGenerationRequest;
import com.example.kafkaeventdrivenapp.model.ReportGenerationResponse;
import com.example.kafkaeventdrivenapp.model.UserRole;
import com.example.kafkaeventdrivenapp.model.MaskedTimesheetData;
import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import com.example.kafkaeventdrivenapp.util.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Service
public class ScheduledReportService {
    
    @Autowired
    private ReportGenerationService reportGenerationService;
    
    @Autowired
    private EmailReportService emailReportService;
    
    
    @Autowired
    private CSVReportGeneratorService csvReportGenerator;
    
    @Autowired
    private SFTPDeliveryService sftpDeliveryService;
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private JobQueueService jobQueueService;
    
    @Autowired
    private CountyCodeMappingService countyCodeMappingService;
    
    @Autowired
    private KeycloakUserService keycloakUserService;
    
    @Autowired
    private FieldMaskingService fieldMaskingService;
    
    @Autowired
    private DataFetchingService dataFetchingService;
    
    @Autowired
    private QueryBuilderService queryBuilderService;
    
    @Value("${report.scheduling.enabled:true}")
    private boolean schedulingEnabled;
    
    @Value("${report.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${report.sftp.enabled:true}")
    private boolean sftpEnabled;
    
    @Value("${keycloak.auth-server-url:http://cmips-keycloak:8080}")
    private String keycloakAuthServerUrl;
    
    @Value("${keycloak.realm:cmips}")
    private String keycloakRealm;
    
    @Value("${keycloak.credentials.secret:UnpJullDQX23tenZ4IsTuGkY8QzBlcFd}")
    private String keycloakClientSecret;
    
    @Value("${keycloak.resource:trial-app}")
    private String keycloakClientId;
    
    @Value("${keycloak.role-token.credentials.system-scheduler.username:system_scheduler}")
    private String systemSchedulerUsername;
    
    @Value("${keycloak.role-token.credentials.system-scheduler.password:system_scheduler_pass_123!}")
    private String systemSchedulerPassword;
    
    @Value("${report.scheduler.system-scheduler.enabled:true}")
    private boolean systemSchedulerEnabled;
    
    @Value("${report.scheduler.system-scheduler.cron:0 0 7 * * ?}")
    private String systemSchedulerCron;
    
    @Value("${report.scheduler.system-scheduler.storage-path:./reports}")
    private String reportStoragePath;
    
    @Value("${batch.test-scheduler.enabled:true}")
    private boolean testSchedulerEnabled;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private com.example.kafkaeventdrivenapp.config.ReportTypeProperties reportTypeProperties;
    
    // Role-specific report configurations - initialized after Spring injection
    private Map<String, CronJobProfile> CRON_PROFILES;
    private Map<UserRole, List<String>> ROLE_ACCESS_MAP;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        CRON_PROFILES = buildRoleReportConfig();
        ROLE_ACCESS_MAP = buildRoleAccessMap(CRON_PROFILES);
    }
    
    private Map<String, CronJobProfile> buildRoleReportConfig() {
        Map<String, CronJobProfile> config = new LinkedHashMap<>();
        
        // Get daily report types from configuration
        List<String> dailyReportTypes = reportTypeProperties.getSchedulers().getDaily();
        if (dailyReportTypes == null || dailyReportTypes.isEmpty()) {
            dailyReportTypes = Arrays.asList("COUNTY_DAILY", "DAILY_SUMMARY");
        }

        config.put("ADMIN_CORE", new CronJobProfile(
                "ADMIN_CORE",
                UserRole.ADMIN,
                Collections.emptyList(),
                new ArrayList<>(dailyReportTypes)
        ));

        // Supervisor profiles - using only the 5 configured counties
        config.put("SUPERVISOR_CORE", new CronJobProfile(
                "SUPERVISOR_CORE",
                UserRole.SUPERVISOR,
                Arrays.asList("Orange", "Sacramento", "Riverside", "Los Angeles", "Alameda"),
                new ArrayList<>(dailyReportTypes)
        ));

        // Case worker profiles using county codes (CT1-CT5)
        config.put("CASE_WORKER_CT1", new CronJobProfile(
                "CASE_WORKER_CT1",
                UserRole.CASE_WORKER,
                List.of("Orange"),
                new ArrayList<>(dailyReportTypes)
        ));

        config.put("CASE_WORKER_CT2", new CronJobProfile(
                "CASE_WORKER_CT2",
                UserRole.CASE_WORKER,
                List.of("Sacramento"),
                new ArrayList<>(dailyReportTypes)
        ));

        config.put("CASE_WORKER_CT3", new CronJobProfile(
                "CASE_WORKER_CT3",
                UserRole.CASE_WORKER,
                List.of("Riverside"),
                new ArrayList<>(dailyReportTypes)
        ));

        config.put("CASE_WORKER_CT4", new CronJobProfile(
                "CASE_WORKER_CT4",
                UserRole.CASE_WORKER,
                List.of("Los Angeles"),
                new ArrayList<>(dailyReportTypes)
        ));

        config.put("CASE_WORKER_CT5", new CronJobProfile(
                "CASE_WORKER_CT5",
                UserRole.CASE_WORKER,
                List.of("Alameda"),
                new ArrayList<>(dailyReportTypes)
        ));

        return config;
    }

    private Map<UserRole, List<String>> buildRoleAccessMap(Map<String, CronJobProfile> profiles) {
        Map<UserRole, List<String>> accessMap = new EnumMap<>(UserRole.class);
        for (CronJobProfile profile : profiles.values()) {
            accessMap.compute(profile.getUserRole(), (role, existing) -> {
                if (existing == null) {
                    return new ArrayList<>(profile.getReportTypes());
                }
                if (!existing.containsAll(profile.getReportTypes())) {
                    List<String> merged = new ArrayList<>(existing);
                    for (String type : profile.getReportTypes()) {
                        if (!merged.contains(type)) {
                            merged.add(type);
                        }
                    }
                    return merged;
                }
                return existing;
            });
        }
        return accessMap;
    }

    public ScheduledReportService() {
        System.out.println("🔧 ScheduledReportService: Initializing unified scheduled report service");
    }
    
    /**
     * Daily report generation - runs at 4:15 PM daily (configurable via application.yml)
     */
    @Scheduled(cron = "${report.scheduling.daily-report-cron:0 30 5 * * ?}", zone = "Asia/Kolkata")
    public void generateDailyReports() {
        if (!schedulingEnabled) {
            System.out.println("⏰ ScheduledReportService: Scheduling disabled, skipping daily reports");
            return;
        }
        
        try {
            System.out.println("⏰ ScheduledReportService: Starting unified daily report generation at " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            System.out.println("📅 Generating daily reports for date: " + dateStr);
            
            int totalReports = 0;
            int successfulReports = 0;
            int failedReports = 0;
            
            // Generate reports for each role with their specific report types
            for (CronJobProfile profile : CRON_PROFILES.values()) {
                List<String> reportTypes = profile.getReportTypes();
                
                System.out.println("👤 Processing reports for role: " + profile.getTokenKey());
                System.out.println("📊 Report types: " + reportTypes);
                
                for (String reportType : reportTypes) {
                    totalReports++;
                    try {
                        // Create scheduled job via JobQueueService (will appear in dashboard)
                        createScheduledJob(profile, reportType, "DAILY", getPreviousDayRange());
                        successfulReports++;
                        System.out.println("✅ Successfully queued scheduled daily job for " + profile.getTokenKey() + "/" + reportType);
                    } catch (Exception e) {
                        failedReports++;
                        System.err.println("❌ Error queuing daily report job for " + profile.getTokenKey() + "/" + reportType + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // Send batch completion notification
            notificationService.sendBatchCompletionNotification(totalReports, successfulReports, failedReports);
            
            System.out.println("✅ ScheduledReportService: Unified daily report generation completed. Success: " + successfulReports + ", Failures: " + failedReports);
            
        } catch (Exception e) {
            System.err.println("❌ Error in daily report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Weekly report generation - runs at 9 AM every Monday
     */
    @Scheduled(cron = "${report.scheduling.weekly-report-cron:0 30 5 * * MON}", zone = "Asia/Kolkata")
    public void generateWeeklyReports() {
        if (!schedulingEnabled) {
            System.out.println("⏰ ScheduledReportService: Scheduling disabled, skipping weekly reports");
            return;
        }
        
        try {
            System.out.println("⏰ ScheduledReportService: Starting weekly report generation at " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            
            // Generate weekly summary reports for central workers only
            int successCount = 0;
            int failureCount = 0;
            
            CronJobProfile centralProfile = findProfileByRole(UserRole.ADMIN);
            if (centralProfile != null) {
                try {
                    // Create scheduled job via JobQueueService - using first weekly report type from config
                    String weeklyReportType = reportTypeProperties.getSchedulers().getWeekly().isEmpty() 
                        ? "WEEKLY_REPORT" 
                        : reportTypeProperties.getSchedulers().getWeekly().get(0);
                    createScheduledJob(centralProfile, weeklyReportType, "WEEKLY", getPreviousWeekRange());
                    successCount++;
                    System.out.println("✅ Successfully queued scheduled weekly job for role: " + centralProfile.getTokenKey());
                } catch (Exception e) {
                    failureCount++;
                    System.err.println("❌ Error queuing weekly report job for role " + centralProfile.getTokenKey() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("⏰ ScheduledReportService: Weekly report generation completed - Success: " + successCount + ", Failures: " + failureCount);
            
        } catch (Exception e) {
            System.err.println("❌ Error in weekly report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Monthly report generation - runs at 9 AM on the 1st day of every month
     */
    @Scheduled(cron = "${report.scheduling.monthly-report-cron:0 30 5 1 * ?}", zone = "Asia/Kolkata")
    public void generateMonthlyReports() {
        if (!schedulingEnabled) {
            System.out.println("⏰ ScheduledReportService: Scheduling disabled, skipping monthly reports");
            return;
        }
        
        try {
            System.out.println("⏰ ScheduledReportService: Starting monthly report generation at " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            
            // Generate monthly reports for all roles
            int successCount = 0;
            int failureCount = 0;
            
            for (CronJobProfile profile : CRON_PROFILES.values()) {
                try {
                    // Using first monthly report type from config
                    String monthlyReportType = reportTypeProperties.getSchedulers().getMonthly().isEmpty() 
                        ? "MONTHLY_REPORT" 
                        : reportTypeProperties.getSchedulers().getMonthly().get(0);
                    createScheduledJob(profile, monthlyReportType, "MONTHLY", getPreviousMonthRange());
                    successCount++;
                    System.out.println("✅ Successfully queued scheduled monthly job for role: " + profile.getTokenKey());
                } catch (Exception e) {
                    failureCount++;
                    System.err.println("❌ Error queuing monthly report job for role " + profile.getTokenKey() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("⏰ ScheduledReportService: Monthly report generation completed - Success: " + successCount + ", Failures: " + failureCount);
            
        } catch (Exception e) {
            System.err.println("❌ Error in monthly report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Quarterly report generation - runs at 9 AM on the 1st day of every quarter (Jan, Apr, Jul, Oct)
     */
    @Scheduled(cron = "${report.scheduling.quarterly-report-cron:0 30 5 1 1,4,7,10 ?}", zone = "Asia/Kolkata")
    public void generateQuarterlyReports() {
        if (!schedulingEnabled) {
            System.out.println("⏰ ScheduledReportService: Scheduling disabled, skipping quarterly reports");
            return;
        }
        
        try {
            System.out.println("⏰ ScheduledReportService: Starting quarterly report generation at " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            
            // Generate quarterly reports for central workers only
            int successCount = 0;
            int failureCount = 0;
            CronJobProfile centralProfile = findProfileByRole(UserRole.ADMIN);
            if (centralProfile != null) {
                try {
                    // Using first quarterly report type from config
                    String quarterlyReportType = reportTypeProperties.getSchedulers().getQuarterly().isEmpty() 
                        ? "QUARTERLY_REPORT" 
                        : reportTypeProperties.getSchedulers().getQuarterly().get(0);
                    createScheduledJob(centralProfile, quarterlyReportType, "QUARTERLY", getPreviousQuarterRange());
                    successCount++;
                    System.out.println("✅ Successfully queued scheduled quarterly job for role: " + centralProfile.getTokenKey());
                } catch (Exception e) {
                    failureCount++;
                    System.err.println("❌ Error queuing quarterly report job for role " + centralProfile.getTokenKey() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("⏰ ScheduledReportService: Quarterly report generation completed - Success: " + successCount + ", Failures: " + failureCount);
            
        } catch (Exception e) {
            System.err.println("❌ Error in quarterly report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Yearly report generation - runs at 9 AM on January 1st
     */
    @Scheduled(cron = "${report.scheduling.yearly-report-cron:0 30 5 1 1 ?}", zone = "Asia/Kolkata")
    public void generateYearlyReports() {
        if (!schedulingEnabled) {
            System.out.println("⏰ ScheduledReportService: Scheduling disabled, skipping yearly reports");
            return;
        }
        
        try {
            System.out.println("⏰ ScheduledReportService: Starting yearly report generation at " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            
            // Generate yearly reports for central workers only
            int successCount = 0;
            int failureCount = 0;
            CronJobProfile centralProfile = findProfileByRole(UserRole.ADMIN);
            if (centralProfile != null) {
                try {
                    // Using first annual report type from config
                    String annualReportType = reportTypeProperties.getSchedulers().getAnnual().isEmpty() 
                        ? "ANNUAL_REPORT" 
                        : reportTypeProperties.getSchedulers().getAnnual().get(0);
                    createScheduledJob(centralProfile, annualReportType, "YEARLY", getPreviousYearRange());
                    successCount++;
                    System.out.println("✅ Successfully queued scheduled yearly job for role: " + centralProfile.getTokenKey());
                } catch (Exception e) {
                    failureCount++;
                    System.err.println("❌ Error queuing yearly report job for role " + centralProfile.getTokenKey() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("⏰ ScheduledReportService: Yearly report generation completed - Success: " + successCount + ", Failures: " + failureCount);
            
        } catch (Exception e) {
            System.err.println("❌ Error in yearly report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate and email report for a specific role
     */
    public boolean generateAndEmailReport(String userRole, String reportType, Map<String, Object> dateRange) {
        System.out.println("🔧 ScheduledReportService: generateAndEmailReport called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use generateAndEmailReport(userRole, reportType, dateRange, jwtToken) with JWT token.");
    }
    
    public boolean generateAndEmailReport(String userRole, String reportType, Map<String, Object> dateRange, String jwtToken) {
        try {
            System.out.println("📊 ScheduledReportService: Generating " + reportType + " report for " + userRole);
            UserRole canonicalRole = UserRole.from(userRole);
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token is required for scheduled report generation. No fallback methods available.");
            }
            
            // Extract county from JWT token - NO FALLBACK
            String userCounty = extractCountyFromJWT(jwtToken);
            if (userCounty == null || userCounty.trim().isEmpty()) {
                throw new RuntimeException("County is required in JWT token (attributes.countyId) for role " + userRole + ". No fallback available.");
            }
            System.out.println("✅ ScheduledReportService: Extracted county from JWT: " + userCounty);
            
            // Create report generation request
            ReportGenerationRequest request = new ReportGenerationRequest();
            request.setUserRole(canonicalRole.name());
            request.setReportType(reportType);
            request.setStartDate((LocalDate) dateRange.get("startDate"));
            request.setEndDate((LocalDate) dateRange.get("endDate"));
            request.setUserCounty(userCounty); // Use county from JWT token
            
            // Generate report using provided JWT token
            ReportGenerationResponse response = reportGenerationService.generateReport(request, jwtToken);
            
            if (!"SUCCESS".equals(response.getStatus())) {
                System.err.println("❌ Report generation failed: " + response.getErrorMessage());
                return false;
            }
            
            // Prepare additional data for email
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("dateRange", dateRange.get("startDate") + " to " + dateRange.get("endDate"));
            additionalData.put("isScheduled", true);
            // Use IST timezone for scheduledAt
            additionalData.put("scheduledAt", java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
            
            // Send email if enabled
            if (emailEnabled) {
                boolean emailSuccess = emailReportService.sendScheduledReportEmail(
                    reportType, 
                    userRole, 
                    response.getData().getRecords(), 
                    additionalData,
                    jwtToken
                );
                
                if (emailSuccess) {
                    System.out.println("✅ Scheduled report email sent successfully for role: " + userRole);
                    return true;
                } else {
                    System.err.println("❌ Failed to send scheduled report email for role: " + userRole);
                    return false;
                }
            } else {
                System.out.println("📧 Email disabled, report generated but not sent for role: " + userRole);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in generateAndEmailReport: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Manually trigger scheduled report generation
     */
    public boolean triggerScheduledReport(String userRole, String reportType) {
        System.out.println("🔧 ScheduledReportService: triggerScheduledReport called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use triggerScheduledReport(userRole, reportType, jwtToken) with JWT token.");
    }
    
    public boolean triggerScheduledReport(String userRole, String reportType, String jwtToken) {
        try {
            System.out.println("🔧 ScheduledReportService: Manually triggering " + reportType + " report for " + userRole);
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token is required for scheduled report triggering. No fallback methods available.");
            }
            
            Map<String, Object> dateRange = getPreviousDayRange();
            return generateAndEmailReport(userRole, reportType, dateRange, jwtToken);
            
        } catch (Exception e) {
            System.err.println("❌ Error triggering scheduled report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate system JWT token from Keycloak service account
     */
    /**
     * Generate system JWT token
     * NOTE: Token generation is now handled by sajeevs-codebase-main's Keycloak services
     * This method is kept for backward compatibility but returns null
     */
    public String generateSystemJwtToken() {
        System.out.println("ℹ️  ScheduledReportService: Token generation is handled by sajeevs-codebase-main Keycloak services");
        return null; // Token should be provided by sajeevs-codebase-main
    }
    
    /**
     * Generate JWT token for a specific profile using cron user credentials
     * Uses profile identifier to generate county-specific username
     */
    private String generateJwtTokenForProfile(CronJobProfile profile) {
        try {
            String username = getCronUsernameForProfile(profile);
            String password = getCronPasswordForProfile(profile);
            
            if (username == null || password == null) {
                System.err.println("❌ ScheduledReportService: No cron credentials configured for profile: " + profile.getTokenKey());
                return null;
            }
            
            String tokenUrl = keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";
            
            String requestBody = "username=" + username +
                               "&password=" + password +
                               "&grant_type=password" +
                               "&client_id=" + keycloakClientId +
                               "&client_secret=" + keycloakClientSecret;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                System.out.println("✅ ScheduledReportService: Successfully generated JWT token for profile: " + profile.getTokenKey());
                return accessToken;
            } else {
                System.err.println("❌ ScheduledReportService: Failed to get token from Keycloak. Status: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ ScheduledReportService: Error generating JWT token for profile " + profile.getTokenKey() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate JWT token for a specific role using cron user credentials (legacy method for backward compatibility)
     */
    private String generateJwtTokenForRole(UserRole role) {
        try {
            String username = getCronUsernameForRole(role);
            String password = getCronPasswordForRole(role);
            
            if (username == null || password == null) {
                System.err.println("❌ ScheduledReportService: No cron credentials configured for role: " + role);
                return null;
            }
            
            String tokenUrl = keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";
            
            String requestBody = "username=" + username +
                               "&password=" + password +
                               "&grant_type=password" +
                               "&client_id=" + keycloakClientId +
                               "&client_secret=" + keycloakClientSecret;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                System.out.println("✅ ScheduledReportService: Successfully generated JWT token for role: " + role);
                return accessToken;
            } else {
                System.err.println("❌ ScheduledReportService: Failed to get token from Keycloak. Status: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ ScheduledReportService: Error generating JWT token for role " + role + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get cron username for a profile - generates county-specific username like caseworker_CT1
     */
    private String getCronUsernameForProfile(CronJobProfile profile) {
        String identifier = profile.getTokenKey();
        UserRole role = profile.getUserRole();
        
        // Extract county code from identifier (e.g., CASE_WORKER_CT1 -> CT1)
        String countyCode = countyCodeMappingService.extractCountyCodeFromUsername(identifier);
        
        if (countyCode != null) {
            // Generate county-specific cron username: cron_{role}_{countyCode}
            String rolePrefix = getRolePrefixForUserRole(role);
            return "cron_" + rolePrefix + "_" + countyCode.toLowerCase();
        }
        
        // Fallback to role-based username for non-county-specific profiles (ADMIN, SUPERVISOR)
        return getCronUsernameForRole(role);
    }
    
    /**
     * Get cron password for a profile
     */
    private String getCronPasswordForProfile(CronJobProfile profile) {
        // For now, use role-based password (can be extended for county-specific passwords if needed)
        return getCronPasswordForRole(profile.getUserRole());
    }
    
    /**
     * Get role prefix for username generation (e.g., ADMIN -> admin, CASE_WORKER -> caseworker)
     */
    private String getRolePrefixForUserRole(UserRole role) {
        switch (role) {
            case ADMIN:
                return "admin";
            case SUPERVISOR:
                return "supervisor";
            case CASE_WORKER:
                return "caseworker";
            default:
                return "caseworker"; // Default fallback
        }
    }
    
    private String getCronUsernameForRole(UserRole role) {
        // Map role to cron username from application.yml (for non-county-specific roles)
        switch (role) {
            case ADMIN:
                return System.getenv("KEYCLOAK_ROLE_ADMIN_USERNAME") != null ? 
                    System.getenv("KEYCLOAK_ROLE_ADMIN_USERNAME") : "cron_admin";
            case SUPERVISOR:
                return System.getenv("KEYCLOAK_ROLE_SUPERVISOR_USERNAME") != null ? 
                    System.getenv("KEYCLOAK_ROLE_SUPERVISOR_USERNAME") : "cron_supervisor";
            case CASE_WORKER:
                return System.getenv("KEYCLOAK_ROLE_CASE_WORKER_USERNAME") != null ? 
                    System.getenv("KEYCLOAK_ROLE_CASE_WORKER_USERNAME") : "cron_case_worker";
            default:
                return "cron_case_worker"; // Default fallback
        }
    }
    
    private String getCronPasswordForRole(UserRole role) {
        // Map role to cron password from application.yml
        switch (role) {
            case ADMIN:
                return System.getenv("KEYCLOAK_ROLE_ADMIN_PASSWORD") != null ? 
                    System.getenv("KEYCLOAK_ROLE_ADMIN_PASSWORD") : "cron_admin_pass_123!";
            case SUPERVISOR:
                return System.getenv("KEYCLOAK_ROLE_SUPERVISOR_PASSWORD") != null ? 
                    System.getenv("KEYCLOAK_ROLE_SUPERVISOR_PASSWORD") : "cron_supervisor_pass_123!";
            case CASE_WORKER:
                return System.getenv("KEYCLOAK_ROLE_CASE_WORKER_PASSWORD") != null ? 
                    System.getenv("KEYCLOAK_ROLE_CASE_WORKER_PASSWORD") : "cron_case_worker_pass_123!";
            default:
                return "cron_case_worker_pass_123!"; // Default fallback
        }
    }
    
    /**
     * Get previous day date range
     * For testing: using today's date instead of yesterday to ensure we have data
     */
    private Map<String, Object> getPreviousDayRange() {
        // Using today's date for testing - change back to yesterday for production
        LocalDate reportDate = LocalDate.now(); // Changed from minusDays(1) for testing
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("startDate", reportDate);
        dateRange.put("endDate", reportDate);
        System.out.println("📅 ScheduledReportService: Using date range: " + reportDate + " (today for testing)");
        return dateRange;
    }
    
    /**
     * Get previous week date range
     */
    private Map<String, Object> getPreviousWeekRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("startDate", startOfWeek);
        dateRange.put("endDate", endOfWeek);
        return dateRange;
    }
    
    /**
     * Get previous month date range
     */
    private Map<String, Object> getPreviousMonthRange() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfPreviousMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfPreviousMonth = today.withDayOfMonth(1).minusDays(1);
        
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("startDate", firstDayOfPreviousMonth);
        dateRange.put("endDate", lastDayOfPreviousMonth);
        return dateRange;
    }
    
    /**
     * Get previous quarter date range
     */
    private Map<String, Object> getPreviousQuarterRange() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        
        // Determine previous quarter
        int previousQuarter = ((currentMonth - 1) / 3) - 1;
        if (previousQuarter < 0) {
            previousQuarter = 3; // Q4 of previous year
            currentYear--;
        }
        
        int quarterStartMonth = (previousQuarter * 3) + 1;
        LocalDate quarterStart = LocalDate.of(currentYear, quarterStartMonth, 1);
        LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);
        
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("startDate", quarterStart);
        dateRange.put("endDate", quarterEnd);
        return dateRange;
    }
    
    /**
     * Get previous year date range
     */
    private Map<String, Object> getPreviousYearRange() {
        LocalDate today = LocalDate.now();
        int previousYear = today.getYear() - 1;
        LocalDate yearStart = LocalDate.of(previousYear, 1, 1);
        LocalDate yearEnd = LocalDate.of(previousYear, 12, 31);
        
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("startDate", yearStart);
        dateRange.put("endDate", yearEnd);
        return dateRange;
    }
    
    /**
     * Create a scheduled job via JobQueueService
     * This ensures scheduled jobs appear in the batch jobs dashboard
     *
     * IMPORTANT: For profiles with multiple counties (like SUPERVISOR_CORE),
     * this method creates ONE job PER COUNTY to ensure each county gets its own data.
     */
    private void createScheduledJob(CronJobProfile profile, String reportType, String scheduleType, Map<String, Object> dateRange) {
        try {
            if (profile == null) {
                throw new IllegalArgumentException("Cron profile cannot be null for scheduled job creation.");
            }

            System.out.println("📋 ScheduledReportService: Creating scheduled job - " + scheduleType + " " + reportType + " for " + profile.getTokenKey());

            // Validate required fields
            if (profile == null || profile.getUserRole() == null) {
                throw new IllegalArgumentException("Profile or userRole cannot be null when creating scheduled job");
            }
            if (reportType == null || reportType.trim().isEmpty()) {
                throw new IllegalArgumentException("reportType cannot be null or empty when creating scheduled job");
            }

            // Check if profile has counties configured
            if (profile.getCounties().isEmpty()) {
                // For profiles without county restrictions (like ADMIN), create one job without county filter
                System.out.println("ℹ️ ScheduledReportService: Profile " + profile.getTokenKey() + " has no county restriction - creating single job");
                createJobForCounty(profile, reportType, scheduleType, dateRange, null);
            } else if (profile.getCounties().size() == 1) {
                // Single county - create one job
                String county = profile.getCounties().get(0);
                System.out.println("📍 ScheduledReportService: Creating job for single county: " + county);
                createJobForCounty(profile, reportType, scheduleType, dateRange, county);
            } else {
                // Multiple counties - create ONE JOB PER COUNTY
                System.out.println("📍 ScheduledReportService: Creating jobs for " + profile.getCounties().size() + " counties");
                for (String county : profile.getCounties()) {
                    System.out.println("   └─ Creating job for county: " + county);
                    createJobForCounty(profile, reportType, scheduleType, dateRange, county);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating scheduled job: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create a single job for a specific county
     */
    private void createJobForCounty(CronJobProfile profile, String reportType, String scheduleType,
                                    Map<String, Object> dateRange, String county) {
        try {
            // Generate JWT token for scheduled job using cron user credentials
            // For multi-county profiles, we need to get a token for the specific county
            String jwtToken;
            if (county != null && profile.getCounties().size() > 1) {
                // For multi-county profiles (like SUPERVISOR), generate token using county-specific cron user
                jwtToken = generateJwtTokenForCounty(profile.getUserRole(), county);
            } else {
                // Use profile tokenKey to get county-specific username
                jwtToken = generateJwtTokenForProfile(profile);
            }

            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("Failed to generate JWT token for scheduled job. Cannot proceed without authentication token.");
            }
            System.out.println("✅ ScheduledReportService: JWT token generated for " + profile.getTokenKey() + (county != null ? " (county: " + county + ")" : ""));

            // Create BIReportRequest
            com.example.kafkaeventdrivenapp.model.BIReportRequest request = new com.example.kafkaeventdrivenapp.model.BIReportRequest();
            request.setUserRole(profile.getUserRole().name());
            request.setReportType(reportType.trim());
            request.setTargetSystem("SCHEDULED");
            request.setDataFormat("JSON"); // Default format, can be configured
            request.setChunkSize(1000);
            request.setPriority(7); // Higher priority for scheduled jobs
            request.setStartDate((LocalDate) dateRange.get("startDate"));
            request.setEndDate((LocalDate) dateRange.get("endDate"));

            // Set county for this scheduled request
            if (county != null) {
                request.setCountyId(county);
            }

            // Queue the job (will create entry in report_jobs table)
            String jobId = jobQueueService.queueReportJob(request, jwtToken);

            // Mark job as scheduled
            jobQueueService.setJobSource(jobId, "SCHEDULED");

            System.out.println("✅ ScheduledReportService: Scheduled job created with ID: " + jobId + (county != null ? " for county: " + county : ""));

        } catch (Exception e) {
            System.err.println("❌ Error creating job for county " + county + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Generate JWT token for a specific county using the appropriate cron user
     * Maps county name to county code and generates token for that county's cron user
     */
    private String generateJwtTokenForCounty(UserRole role, String countyName) {
        try {
            // Get county code from county name
            String countyCode = countyCodeMappingService.getCountyCode(countyName);
            if (countyCode == null) {
                System.err.println("❌ ScheduledReportService: No county code found for: " + countyName);
                return null;
            }

            // Generate username: cron_{role}_{countyCode}
            String rolePrefix = getRolePrefixForUserRole(role);
            String username = "cron_" + rolePrefix + "_" + countyCode.toLowerCase();
            String password = getCronPasswordForRole(role);

            System.out.println("🔐 ScheduledReportService: Generating token for county user: " + username);

            String tokenUrl = keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";

            String requestBody = "username=" + username +
                               "&password=" + password +
                               "&grant_type=password" +
                               "&client_id=" + keycloakClientId +
                               "&client_secret=" + keycloakClientSecret;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                System.out.println("✅ ScheduledReportService: Successfully generated JWT token for county user: " + username);
                return accessToken;
            } else {
                System.err.println("❌ ScheduledReportService: Failed to get token for county user " + username + ". Status: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ ScheduledReportService: Error generating JWT token for county " + countyName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get county for role/identifier - uses CountyCodeMappingService to handle county codes
     */
    private String getCountyForRole(String identifier) {
        if (identifier == null) return "Alameda";

        // First, try to extract county code from identifier (e.g., CASE_WORKER_CT1 -> CT1)
        String countyCode = countyCodeMappingService.extractCountyCodeFromUsername(identifier);
        if (countyCode != null) {
            String countyName = countyCodeMappingService.getCountyName(countyCode);
            if (countyName != null) {
                return countyName;
            }
        }

        // Fallback to legacy county name inference
        String inferred = inferCountyFromIdentifier(identifier);
        if (inferred != null) {
            return inferred;
        }

        return "Alameda"; // Default fallback
    }
    
    private String inferCountyFromIdentifier(String identifier) {
        String upper = identifier.toUpperCase();
        
        // Check for county codes first (CT1-CT5)
        for (String code : countyCodeMappingService.getAllCountyCodes()) {
            if (upper.contains(code)) {
                String countyName = countyCodeMappingService.getCountyName(code);
                if (countyName != null) {
                    return countyName;
                }
            }
        }
        
        // Only support the 5 configured counties
        if (upper.contains("ORANGE")) return "Orange";
        if (upper.contains("LOS_ANGELES") || upper.contains("LOSANGELES")) return "Los Angeles";
        if (upper.contains("ALAMEDA")) return "Alameda";
        if (upper.contains("SACRAMENTO")) return "Sacramento";
        if (upper.contains("RIVERSIDE")) return "Riverside";
        
        return null;
    }
    
    /**
     * Extract county from JWT token - ensures reports are filtered by user's assigned county
     */
    private String extractCountyFromJWT(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            // Extract countyId from attributes (Keycloak custom attributes)
            if (jsonNode.has("countyId")) {
                return jsonNode.get("countyId").asText();
            } else if (jsonNode.has("attributes") && jsonNode.get("attributes").has("countyId")) {
                com.fasterxml.jackson.databind.JsonNode countyIdNode = jsonNode.get("attributes").get("countyId");
                if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                    return countyIdNode.get(0).asText();
                } else if (countyIdNode.isTextual()) {
                    return countyIdNode.asText();
                }
            }
            
            // NO FALLBACK - countyId MUST be in JWT token
            System.err.println("❌ ScheduledReportService: countyId NOT FOUND in JWT token. Token must contain countyId in attributes.countyId.");
            java.util.List<String> fieldNames = new java.util.ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(fieldNames::add);
            System.err.println("❌ ScheduledReportService: JWT payload keys: " + fieldNames);
            return null; // Explicitly fail - no default
        } catch (Exception e) {
            System.err.println("⚠️ ScheduledReportService: Error extracting county from JWT: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate unified report for a specific role and report type
     * Handles both PDF (email) and CSV (SFTP) delivery methods
     */
    private void generateUnifiedReportForRole(String userRole, String reportType, String dateStr) {
        System.out.println("📊 Generating unified report: " + userRole + " - " + reportType);
        System.out.println("🔍 Role-based data filtering will be applied for: " + userRole);
        
        try {
            // Validate role permissions before generating report
            if (!isValidRoleForReport(userRole, reportType)) {
                System.out.println("⚠️ Skipping report - role " + userRole + " not authorized for " + reportType);
                return;
            }
            
            // Generate system JWT token for scheduled reports
            String systemJwtToken = generateSystemJwtToken();
            if (systemJwtToken == null) {
                System.err.println("❌ Failed to generate system JWT token for scheduled report");
                return;
            }
            
            // 1. Generate PDF Report (Email Delivery)
            if (emailEnabled) {
                try {
                    System.out.println("📧 Generating PDF report for email delivery...");
                    generateAndEmailReport(userRole, reportTypeProperties.getDefaultReportType(), getPreviousDayRange(), systemJwtToken);
                    System.out.println("✅ PDF report generated and emailed for " + userRole);
                } catch (Exception e) {
                    System.err.println("❌ Error generating PDF report for " + userRole + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 2. Generate CSV Report (SFTP Delivery)
            if (sftpEnabled) {
                try {
                    System.out.println("📄 Generating CSV report for SFTP delivery...");
                    String csvFilePath = csvReportGenerator.generateDailyCSVReportDirect(userRole, reportType, dateStr);
                    System.out.println("📄 CSV report generated: " + csvFilePath);
                    
                    // Encrypt the CSV file
                    String encryptedFilePath = encryptionService.encryptFile(csvFilePath);
                    System.out.println("🔒 CSV report encrypted: " + encryptedFilePath);
                    
                    // Deliver to SFTP server
                    String sftpPath = sftpDeliveryService.deliverFile(encryptedFilePath, userRole, reportType, dateStr);
                    System.out.println("📤 File delivered to SFTP: " + sftpPath);
                    
                    // Send success notification
                    notificationService.sendDeliveryNotification(
                        userRole, 
                        reportType, 
                        sftpPath, 
                        "Daily report successfully delivered with role-based data filtering"
                    );
                    
                } catch (Exception e) {
                    System.err.println("❌ Error generating CSV report for " + userRole + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating unified report for " + userRole + "/" + reportType + ": " + e.getMessage());
            e.printStackTrace();
            
            // Send error notification
            notificationService.sendErrorNotification(
                "Unified Report Generation Failed for " + userRole + "/" + reportType,
                "Error: " + e.getMessage()
            );
        }
    }
    
    /**
     * Validate if a role is authorized for a specific report type
     */
    private boolean isValidRoleForReport(String userRole, String reportType) {
        List<String> allowedReportTypes = ROLE_ACCESS_MAP.get(UserRole.from(userRole));
        return allowedReportTypes != null && allowedReportTypes.contains(reportType);
    }

    private CronJobProfile findProfileByRole(UserRole role) {
        return CRON_PROFILES.values().stream()
                .filter(profile -> profile.getUserRole() == role)
                .findFirst()
                .orElse(null);
    }
    
    private static class CronJobProfile {
        private final String tokenKey;
        private final UserRole userRole;
        private final List<String> counties;
        private final List<String> reportTypes;

        CronJobProfile(String tokenKey, UserRole userRole, List<String> counties, List<String> reportTypes) {
            this.tokenKey = tokenKey;
            this.userRole = userRole;
            this.counties = counties != null ? counties : Collections.emptyList();
            this.reportTypes = reportTypes;
        }

        public String getTokenKey() {
            return tokenKey;
        }

        public UserRole getUserRole() {
            return userRole;
        }

        public List<String> getCounties() {
            return counties;
        }

        public List<String> getReportTypes() {
            return reportTypes;
        }
    }
    
    
    /**
     * Generate SYSTEM_SCHEDULER JWT token using credentials from .env
     */
    private String generateSystemSchedulerToken() {
        try {
            System.out.println("🔐 ScheduledReportService: Generating SYSTEM_SCHEDULER token...");
            
            String tokenUrl = keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";
            
            String requestBody = "username=" + systemSchedulerUsername +
                               "&password=" + systemSchedulerPassword +
                               "&grant_type=password" +
                               "&client_id=" + keycloakClientId +
                               "&client_secret=" + keycloakClientSecret;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                System.out.println("✅ ScheduledReportService: Successfully generated SYSTEM_SCHEDULER token");
                return accessToken;
            } else {
                System.err.println("❌ ScheduledReportService: Failed to get SYSTEM_SCHEDULER token from Keycloak. Status: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ ScheduledReportService: Error generating SYSTEM_SCHEDULER token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Scheduled batch job for SYSTEM_SCHEDULER to generate county-specific reports
     */
    // Counter to limit runs - reset for new testing
    private static int schedulerRunCount = 0;
    private static final int MAX_SCHEDULER_RUNS = 10; // Increased to allow more test runs
    
    /**
     * Reset scheduler run count (for testing)
     */
    public static void resetSchedulerRunCount() {
        schedulerRunCount = 0;
        System.out.println("🔄 ScheduledReportService: Scheduler run count reset to 0");
    }
    
    @Scheduled(cron = "${report.scheduler.system-scheduler.cron:0 */5 * * * ?}", zone = "Asia/Kolkata")
    public void generateCountyReportsForScheduler() {
        if (!systemSchedulerEnabled || !schedulingEnabled) {
            System.out.println("⏰ ScheduledReportService: SYSTEM_SCHEDULER batch processing disabled, skipping");
            return;
        }
        
        // Stop after MAX_SCHEDULER_RUNS (increased for testing)
        if (schedulerRunCount >= MAX_SCHEDULER_RUNS) {
            System.out.println("⏰ ScheduledReportService: Reached maximum run count (" + MAX_SCHEDULER_RUNS + "), stopping scheduler");
            return;
        }
        
        schedulerRunCount++;
        System.out.println("⏰ ScheduledReportService: Run " + schedulerRunCount + " of " + MAX_SCHEDULER_RUNS);
        
        try {
            System.out.println("⏰ ScheduledReportService: Starting SYSTEM_SCHEDULER county report generation at " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            
            // Get SYSTEM_SCHEDULER token
            String systemToken = generateSystemSchedulerToken();
            if (systemToken == null || systemToken.trim().isEmpty()) {
                System.err.println("❌ ScheduledReportService: Failed to get SYSTEM_SCHEDULER token, aborting batch job");
                return;
            }
            
            // Get all county codes
            List<String> countyCodes = countyCodeMappingService.getAllCountyCodes();
            System.out.println("📋 ScheduledReportService: Generating reports for " + countyCodes.size() + " counties");
            
            int successCount = 0;
            int failureCount = 0;
            
            // Generate reports for each county
            for (String countyCode : countyCodes) {
                String countyName = countyCodeMappingService.getCountyName(countyCode);
                if (countyName == null) {
                    System.err.println("⚠️ ScheduledReportService: County name not found for code: " + countyCode);
                    continue;
                }
                
                try {
                    generateCountySpecificReports(countyCode, countyName, systemToken);
                    successCount++;
                    System.out.println("✅ ScheduledReportService: Successfully generated reports for " + countyCode + " (" + countyName + ")");
                } catch (Exception e) {
                    failureCount++;
                    System.err.println("❌ ScheduledReportService: Error generating reports for " + countyCode + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("✅ ScheduledReportService: SYSTEM_SCHEDULER batch processing completed. Success: " + successCount + ", Failures: " + failureCount);
            
        } catch (Exception e) {
            System.err.println("❌ ScheduledReportService: Error in SYSTEM_SCHEDULER batch processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate county-specific reports for both CASE_WORKER and SUPERVISOR recipients
     */
    private void generateCountySpecificReports(String countyCode, String countyName, String systemToken) {
        System.out.println("📊 ScheduledReportService: Generating reports for county: " + countyCode + " (" + countyName + ")");
        
        // Get date range - use August 2025 to December 2025 to match generated data
        LocalDate startDate = LocalDate.of(2025, 8, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        System.out.println("📅 ScheduledReportService: Using date range: " + startDate + " to " + endDate);
        
        // Report types to generate - from configuration
        List<String> reportTypes = reportTypeProperties.getSchedulers().getDaily();
        if (reportTypes == null || reportTypes.isEmpty()) {
            reportTypes = Arrays.asList("COUNTY_DAILY", "DAILY_SUMMARY");
        }
        
        // Recipient roles
        List<String> recipientRoles = Arrays.asList("CASE_WORKER", "SUPERVISOR");
        
        for (String reportType : reportTypes) {
            for (String recipientRole : recipientRoles) {
                try {
                    generateReportForRecipient(countyCode, countyName, reportType, recipientRole, startDate, endDate, systemToken);
                } catch (Exception e) {
                    System.err.println("❌ ScheduledReportService: Error generating " + reportType + " for " + recipientRole + " in " + countyCode + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Generate a report for a specific recipient role with appropriate field masking
     */
    private void generateReportForRecipient(String countyCode, String countyName, String reportType, 
                                           String recipientRole, LocalDate startDate, LocalDate endDate, 
                                           String systemToken) {
        System.out.println("📊 ScheduledReportService: Generating " + reportType + " for " + recipientRole + " in " + countyCode + " (" + countyName + ")");
        System.out.println("🔍 ScheduledReportService: countyCode=" + countyCode + ", countyName=" + countyName);
        
        try {
            // Step 1: Fetch raw data using SYSTEM_SCHEDULER token (bypasses county restrictions)
            // We need to use DataFetchingService directly to get raw TimesheetEntity objects
            QueryBuilderService.QueryParameters queryParams = new QueryBuilderService.QueryParameters();
            queryParams.setUserRole(UserRole.SYSTEM_SCHEDULER.name());
            queryParams.setCountyId(countyName); // Filter by county
            queryParams.setStartDate(startDate);
            queryParams.setEndDate(endDate);
            
            System.out.println("🔍 ScheduledReportService: Query params - countyId: " + queryParams.getCountyId() + ", startDate: " + startDate + ", endDate: " + endDate);
            
            // Fetch raw data
            DataFetchingService.DataFetchResult fetchResult = dataFetchingService.fetchData(queryParams);
            List<TimesheetEntity> rawData = fetchResult.getData();
            
            System.out.println("🔍 ScheduledReportService: Fetched " + (rawData != null ? rawData.size() : 0) + " records for county: " + countyName);
            
            if (rawData == null || rawData.isEmpty()) {
                System.out.println("⚠️ ScheduledReportService: No data found for " + countyCode + ", skipping report");
                return;
            }
            
            System.out.println("📊 ScheduledReportService: Fetched " + rawData.size() + " raw records for " + countyCode);
            
            // Step 2: Apply field masking based on recipient role (not SYSTEM_SCHEDULER)
            List<MaskedTimesheetData> maskedData = fieldMaskingService.applyFieldMaskingForRecipient(
                rawData, recipientRole, reportType, systemToken
            );
            
            System.out.println("🔒 ScheduledReportService: Applied " + recipientRole + " masking to " + maskedData.size() + " records");
            
            // Step 3: Convert masked data to report format
            List<Map<String, Object>> reportRecords = maskedData.stream()
                .map(masked -> {
                    Map<String, Object> record = new HashMap<>();
                    record.putAll(masked.getFields());
                    return record;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Step 4: Create job in queue first (so it appears in frontend)
            // Validate required fields before creating job
            if (recipientRole == null || recipientRole.trim().isEmpty()) {
                throw new IllegalArgumentException("recipientRole cannot be null or empty when creating scheduled job");
            }
            if (reportType == null || reportType.trim().isEmpty()) {
                throw new IllegalArgumentException("reportType cannot be null or empty when creating scheduled job");
            }
            
            BIReportRequest jobRequest = new BIReportRequest();
            jobRequest.setUserRole(recipientRole.trim()); // Use recipient role for the job
            jobRequest.setReportType(reportType.trim());
            jobRequest.setTargetSystem("SCHEDULED");
            jobRequest.setDataFormat("JSON");
            jobRequest.setCountyId(countyName);
            jobRequest.setStartDate(startDate);
            jobRequest.setEndDate(endDate);
            
            String jobId = jobQueueService.queueReportJob(jobRequest, systemToken);
            System.out.println("📋 ScheduledReportService: Created job " + jobId + " for " + reportType + " - " + recipientRole + " in " + countyCode);
            
            // Verify job was created with correct fields
            com.example.kafkaeventdrivenapp.model.JobStatus createdJob = jobQueueService.getJobStatus(jobId);
            if (createdJob != null) {
                System.out.println("✅ ScheduledReportService: Job " + jobId + " created with userRole: " + createdJob.getUserRole() + ", reportType: " + createdJob.getReportType());
            }
            
            // Step 5: Save report to file system
            String fileName = saveReportToFile(countyCode, reportType, recipientRole, reportRecords, startDate);
            System.out.println("✅ ScheduledReportService: Saved report to: " + fileName);
            
            // Step 6: Update job with result path and mark as completed
            jobQueueService.setJobResult(jobId, fileName);
            System.out.println("✅ ScheduledReportService: Job " + jobId + " marked as completed");
            
        } catch (Exception e) {
            System.err.println("❌ ScheduledReportService: Error generating report for recipient: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Save report to file system
     */
    private String saveReportToFile(String countyCode, String reportType, String recipientRole, 
                                   List<Map<String, Object>> data, LocalDate reportDate) {
        try {
            // Create directory structure: reports/{date}/{countyCode}/
            String dateStr = reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            java.io.File dateDir = new java.io.File(reportStoragePath, dateStr);
            java.io.File countyDir = new java.io.File(dateDir, countyCode);
            countyDir.mkdirs();
            
            // Generate filename: {reportType}_{countyCode}_{recipientRole}_{timestamp}.json
            String timestamp = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s_%s_%s.json", reportType, countyCode, recipientRole, timestamp);
            java.io.File reportFile = new java.io.File(countyDir, fileName);
            
            // Convert data to JSON and save
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.writeValue(reportFile, data);
            
            return reportFile.getAbsolutePath();
            
        } catch (Exception e) {
            System.err.println("❌ ScheduledReportService: Error saving report to file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save report to file", e);
        }
    }
    
    /**
     * Get scheduled report service status
     */
    public String getScheduledReportServiceStatus() {
        return String.format(
            "Scheduled Report Service Status:\n" +
            "Service: Active\n" +
            "Scheduling Enabled: %s\n" +
            "Email Enabled: %s\n" +
            "SYSTEM_SCHEDULER Enabled: %s\n" +
            "Daily Reports: 3:00 AM\n" +
            "Weekly Reports: 9:00 AM Monday\n" +
            "Supported Roles: 7 roles\n" +
            "Report Types: TIMESHEET, PAYROLL",
            schedulingEnabled,
            emailEnabled,
            systemSchedulerEnabled
        );
    }
    
    /**
     * TEST SCHEDULER: Triggers jobs every 2 minutes for 10 minutes (5 jobs total)
     * Each job is a DAILY_REPORT for CASE_WORKER which will trigger DAILY_SUMMARY as dependent job
     * 
     * This is a test method to verify:
     * 1. Jobs are triggered at 2-minute intervals
     * 2. Dependent jobs are automatically triggered after parent jobs complete
     */
    // Test scheduler state
    private static int testSchedulerRunCount = 0;
    private static final int MAX_TEST_RUNS = 5; // 10 minutes / 2 minutes = 5 jobs
    private static boolean testSchedulerActive = false;
    private static long testSchedulerStartTime = 0;
    
    /**
     * Start the test scheduler (triggers jobs every 2 minutes for 10 minutes)
     * Can be called manually or via API
     */
    public void startTestScheduler() {
        if (testSchedulerActive) {
            System.out.println("⚠️ Test scheduler is already running");
            return;
        }
        
        testSchedulerActive = true;
        testSchedulerRunCount = 0;
        testSchedulerStartTime = System.currentTimeMillis();
        
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("🚀 TEST SCHEDULER STARTED");
        System.out.println("   Duration: 10 minutes");
        System.out.println("   Interval: 2 minutes between jobs");
        System.out.println("   Total Jobs: 5");
        System.out.println("   Job Type: DAILY_REPORT (CASE_WORKER)");
        System.out.println("   Expected Dependent Jobs: DAILY_SUMMARY (CASE_WORKER)");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        // Trigger first job immediately
        triggerTestJob();
        
        // Schedule subsequent jobs
        scheduleTestJobs();
    }
    
    /**
     * Stop the test scheduler
     */
    public void stopTestScheduler() {
        testSchedulerActive = false;
        testSchedulerRunCount = 0;
        System.out.println("⏹️  TEST SCHEDULER STOPPED");
    }
    
    /**
     * Reset test scheduler counters
     */
    public void resetTestScheduler() {
        testSchedulerRunCount = 0;
        testSchedulerActive = false;
        testSchedulerStartTime = 0;
        System.out.println("🔄 TEST SCHEDULER RESET");
    }
    
    /**
     * Scheduled method that runs every 2 minutes to trigger test jobs
     * Uses existing createScheduledJob method to create jobs for both CASE_WORKER and SUPERVISOR
     */
    @Scheduled(fixedRateString = "${batch.test-scheduler.interval-ms:120000}") // 2 minutes = 120000ms
    public void triggerTestJobScheduled() {
        if (!testSchedulerEnabled) {
            return;
        }
        
        // Check if we've reached max runs (5 runs = 10 minutes)
        if (testSchedulerRunCount >= MAX_TEST_RUNS) {
            return; // Silently stop after 5 runs
        }
        
        triggerTestJob();
    }
    
    /**
     * Schedule test jobs using a background thread
     */
    private void scheduleTestJobs() {
        new Thread(() -> {
            try {
                // Wait 2 minutes between each job (5 jobs total over 10 minutes)
                for (int i = 1; i < MAX_TEST_RUNS; i++) {
                    Thread.sleep(120000); // 2 minutes = 120000ms
                    
                    if (!testSchedulerActive) {
                        break;
                    }
                    
                    triggerTestJob();
                }
                
                // After 10 minutes, stop the scheduler
                Thread.sleep(120000); // Wait for last job
                if (testSchedulerActive) {
                    System.out.println("═══════════════════════════════════════════════════════════════");
                    System.out.println("✅ TEST SCHEDULER COMPLETED");
                    System.out.println("   Total Jobs Triggered: " + testSchedulerRunCount);
                    System.out.println("   Duration: 10 minutes");
                    System.out.println("═══════════════════════════════════════════════════════════════");
                    stopTestScheduler();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Test scheduler thread interrupted");
            }
        }).start();
    }
    
    /**
     * Trigger test jobs for both CASE_WORKER and SUPERVISOR
     * Uses existing createScheduledJob method to reuse existing infrastructure
     * CASE_WORKER jobs will trigger DAILY_SUMMARY as dependent jobs when they complete
     */
    private void triggerTestJob() {
        testSchedulerRunCount++;
        
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("🧪 [TEST BATCH JOB] Triggering test jobs #" + testSchedulerRunCount + " of " + MAX_TEST_RUNS);
        System.out.println("   Time: " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
        System.out.println("   Creating jobs for: CASE_WORKER and SUPERVISOR");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        try {
            // Use existing createScheduledJob method - creates jobs for CASE_WORKER
            CronJobProfile caseWorkerProfile = CRON_PROFILES.get("CASE_WORKER_CT1");
            if (caseWorkerProfile != null) {
                System.out.println("   ┌─ Creating job for CASE_WORKER (Orange county)");
                // CRITICAL FIX: Changed from DAILY_REPORT to COUNTY_DAILY to match dependency config
                String testReportType = reportTypeProperties.getSchedulers().getTest().isEmpty() 
                    ? "COUNTY_DAILY" 
                    : reportTypeProperties.getSchedulers().getTest().get(0);
                createScheduledJob(caseWorkerProfile, testReportType, "TEST", getPreviousDayRange());
                System.out.println("   └─ CASE_WORKER job created - will be visible to CASE_WORKER users in Orange county");
                System.out.println("      └─ Will trigger DAILY_SUMMARY dependent job when it completes");
            } else {
                System.err.println("   ❌ CASE_WORKER_CT1 profile not found");
            }
            
            // Create job for SUPERVISOR using existing method
            CronJobProfile supervisorProfile = CRON_PROFILES.get("SUPERVISOR_CORE");
            if (supervisorProfile != null) {
                System.out.println("   ┌─ Creating job for SUPERVISOR");
                // CRITICAL FIX: Changed from DAILY_REPORT to COUNTY_DAILY to match dependency config
                String testReportType = reportTypeProperties.getSchedulers().getTest().isEmpty() 
                    ? "COUNTY_DAILY" 
                    : reportTypeProperties.getSchedulers().getTest().get(0);
                createScheduledJob(supervisorProfile, testReportType, "TEST", getPreviousDayRange());
                System.out.println("   └─ SUPERVISOR job created - will be visible to SUPERVISOR users");
            } else {
                System.err.println("   ❌ SUPERVISOR_CORE profile not found");
            }
            
            System.out.println("✅ Test scheduler run #" + testSchedulerRunCount + " completed");
            System.out.println("   └─ Jobs will appear on CASE_WORKER and SUPERVISOR screens");
            System.out.println("═══════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("❌ TEST SCHEDULER: Error triggering jobs #" + testSchedulerRunCount + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get test scheduler status
     */
    public String getTestSchedulerStatus() {
        if (!testSchedulerActive) {
            return "Test Scheduler: INACTIVE";
        }
        
        long elapsed = System.currentTimeMillis() - testSchedulerStartTime;
        long elapsedMinutes = elapsed / 60000;
        long remainingMinutes = 10 - elapsedMinutes;
        
        return String.format(
            "Test Scheduler Status:\n" +
            "Status: ACTIVE\n" +
            "Jobs Triggered: %d / %d\n" +
            "Elapsed Time: %d minutes\n" +
            "Remaining Time: %d minutes\n" +
            "Job Type: DAILY_REPORT (CASE_WORKER)\n" +
            "Dependent Job: DAILY_SUMMARY (CASE_WORKER)",
            testSchedulerRunCount,
            MAX_TEST_RUNS,
            elapsedMinutes,
            remainingMinutes
        );
    }
}
