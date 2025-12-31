package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.entity.ReportJobEntity;
import com.example.kafkaeventdrivenapp.model.*;
import com.example.kafkaeventdrivenapp.repository.ReportJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BackgroundProcessingService {
    
    @Autowired
    private ReportJobRepository jobRepository;
    
    @Autowired
    private ReportGenerationService reportGenerationService;
    
    @Autowired
    private JobQueueService jobQueueService;
    
    @Autowired
    private PDFReportGeneratorService pdfReportGeneratorService;
    
    @Autowired
    private DataFetchingService dataFetchingService;
    
    @Autowired
    private QueryBuilderService queryBuilderService;
    
    @Autowired(required = false)
    private JobDependencyService jobDependencyService;
    
    public BackgroundProcessingService() {
        System.out.println("🔧 BackgroundProcessingService: Constructor called - initializing...");
        try {
            System.out.println("✅ BackgroundProcessingService: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ BackgroundProcessingService: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Process a queued job by ID (invoked by scheduler/worker).
     */
    public void processJob(String jobId) {
        try {
            // Get job from database
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            if (jobOpt.isEmpty()) {
                System.err.println("❌ [JOB ERROR] Job not found in database: " + jobId);
                return;
            }
            
            ReportJobEntity job = jobOpt.get();
            BIReportRequest originalRequest = jobQueueService.deserializeRequest(job);
            if (originalRequest == null) {
                originalRequest = new BIReportRequest();
            }
            
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("🚀 [JOB STARTED] Job ID: " + jobId);
            System.out.println("   Report Type: " + job.getReportType());
            System.out.println("   User Role: " + job.getUserRole());
            System.out.println("   Target System: " + job.getTargetSystem());
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            // Process the report
            processReportInBackground(job, originalRequest);
            
        } catch (Exception e) {
            // Try to get job info for better error logging
            Optional<ReportJobEntity> failedJobOpt = jobRepository.findByJobId(jobId);
            System.err.println("═══════════════════════════════════════════════════════════════");
            System.err.println("❌ [JOB FAILED] Job ID: " + jobId);
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   Status: PROCESSING → FAILED");
            if (failedJobOpt.isPresent() && failedJobOpt.get().getParentJobId() != null) {
                System.err.println("   Parent Job ID: " + failedJobOpt.get().getParentJobId());
                System.err.println("   ⚠️  This was a dependent job - parent job status not affected");
            }
            System.err.println("═══════════════════════════════════════════════════════════════");
            e.printStackTrace();
            
            // Update job status to failed
            jobQueueService.updateJobStatus(jobId, "FAILED", e.getMessage());
        }
    }
    
    /**
     * Process report in background with chunked data processing
     */
    private void processReportInBackground(ReportJobEntity job, BIReportRequest originalRequest) {
        System.out.println("📊 BackgroundProcessingService: Processing report in background for job: " + job.getJobId());
        
        try {
            // Check if job was cancelled
            Optional<ReportJobEntity> currentJob = jobRepository.findByJobId(job.getJobId());
            if (currentJob.isPresent() && "CANCELLED".equals(currentJob.get().getStatus())) {
                System.out.println("⚠️ Job was cancelled: " + job.getJobId());
                return;
            }
            
            // Extract JWT token stored with the job
            String jwtToken = job.getJwtToken();
            
            // Extract user info from JWT token for authentication and permissions
            // NOTE: JWT parsing is now handled directly since KeycloakService is removed
            String userRole = job.getUserRole();
            String countyId = null;
            
            if (jwtToken != null && !jwtToken.trim().isEmpty()) {
                System.out.println("🔐 BackgroundProcessingService: Extracting user info from JWT token");
                try {
                    // Parse JWT token to extract role
                    String[] parts = jwtToken.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
                        
                        // Extract role from realm_access or resource_access
                        if (jsonNode.has("realm_access") && jsonNode.get("realm_access").has("roles")) {
                            com.fasterxml.jackson.databind.JsonNode roles = jsonNode.get("realm_access").get("roles");
                            if (roles.isArray() && roles.size() > 0) {
                                for (int i = 0; i < roles.size(); i++) {
                                    String tokenRole = roles.get(i).asText();
                                    // Skip system roles, get the actual user role
                                    if (tokenRole != null && !tokenRole.startsWith("default-roles")
                                        && !"offline_access".equals(tokenRole)
                                        && !"uma_authorization".equals(tokenRole)) {
                                        userRole = tokenRole;
                                        break;
                                    }
                                }
                            }
                        }

                        // Extract countyId from JWT token - check multiple possible claim names
                        // Keycloak user attributes are mapped as direct claims
                        if (jsonNode.has("countyId")) {
                            com.fasterxml.jackson.databind.JsonNode countyIdNode = jsonNode.get("countyId");
                            if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                                countyId = countyIdNode.get(0).asText();
                            } else if (countyIdNode.isTextual()) {
                                countyId = countyIdNode.asText();
                            }
                            System.out.println("✅ BackgroundProcessingService: Extracted countyId from JWT (countyId claim): " + countyId);
                        } else if (jsonNode.has("county_id")) {
                            countyId = jsonNode.get("county_id").asText();
                            System.out.println("✅ BackgroundProcessingService: Extracted countyId from JWT (county_id claim): " + countyId);
                        }

                        System.out.println("✅ BackgroundProcessingService: User info from token - role: " + userRole + ", countyId: " + countyId);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ BackgroundProcessingService: Could not parse JWT token, using job role: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ BackgroundProcessingService: No JWT token provided, using job request data");
            }
            
            // Fallback to request data if token doesn't provide county context
            if ((countyId == null || countyId.trim().isEmpty()) && originalRequest != null) {
                countyId = originalRequest.getCountyId();
                System.out.println("✅ BackgroundProcessingService: Using countyId from request data: " + countyId);
            }

            System.out.println("🔍 BackgroundProcessingService: Final countyId for job: " + countyId);
            // Create extraction request with token-based user info
            PipelineExtractionRequest extractionRequest = createExtractionRequest(job, originalRequest, userRole, countyId);
            
            // Process data in chunks with streaming file generation
            System.out.println("📊 [JOB PROCESSING] " + job.getJobId() + ": Starting data processing...");
            String resultPath = processDataInChunksStreaming(extractionRequest, job, jwtToken);
            
            // Update job with result (marks job as COMPLETED)
            jobQueueService.setJobResult(job.getJobId(), resultPath);
            
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("✅ [JOB COMPLETED] Job ID: " + job.getJobId());
            System.out.println("   Report Type: " + job.getReportType());
            System.out.println("   Status: PROCESSING → COMPLETED");
            System.out.println("   Result Path: " + resultPath);
            if (job.getParentJobId() != null) {
                System.out.println("   Parent Job ID: " + job.getParentJobId());
                System.out.println("   └─ This was a dependent job");
            }
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            // Small delay to ensure database transaction is committed
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Trigger dependent jobs if dependency service is available
            triggerDependentJobs(job, jwtToken);
            
        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════════════════════════");
            System.err.println("❌ [JOB PROCESSING ERROR] Job ID: " + job.getJobId());
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   Status: PROCESSING → FAILED");
            if (job.getParentJobId() != null) {
                System.err.println("   Parent Job ID: " + job.getParentJobId());
                System.err.println("   ⚠️  This was a dependent job - parent job status not affected");
            }
            System.err.println("═══════════════════════════════════════════════════════════════");
            e.printStackTrace();
            jobQueueService.updateJobStatus(job.getJobId(), "FAILED", e.getMessage());
        }
    }
    
    /**
     * Trigger dependent jobs after a job completes successfully
     * Errors in dependency triggering are logged but don't affect the parent job
     */
    private void triggerDependentJobs(ReportJobEntity completedJob, String jwtToken) {
        String parentJobId = completedJob.getJobId();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔍 [DEPENDENCY CHECK] Checking for dependent jobs...");
        System.out.println("   Parent Job ID: " + parentJobId);
        
        if (jobDependencyService == null) {
            System.out.println("ℹ️  [DEPENDENCY CHECK] JobDependencyService not available, skipping dependency check");
            return;
        }
        
        try {
            // IMPORTANT: Reload job from database to get latest status (entity passed in may be stale)
            Optional<ReportJobEntity> latestJobOpt = jobRepository.findByJobId(parentJobId);
            if (latestJobOpt.isEmpty()) {
                System.out.println("⚠️  [DEPENDENCY CHECK] Parent job not found in database: " + parentJobId);
                return;
            }
            
            ReportJobEntity latestJobEntity = latestJobOpt.get();
            
            System.out.println("   Parent Report Type: " + latestJobEntity.getReportType());
            System.out.println("   Parent User Role: " + latestJobEntity.getUserRole());
            System.out.println("   Parent Status: " + latestJobEntity.getStatus());
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // Only trigger dependencies if job completed successfully
            if (!"COMPLETED".equals(latestJobEntity.getStatus())) {
                System.out.println("⏸️  [DEPENDENCY CHECK] Parent job did not complete successfully");
                System.out.println("   Status: " + latestJobEntity.getStatus());
                System.out.println("   └─ Dependent jobs will NOT be triggered (only triggered on SUCCESS)");
                return;
            }
            
            System.out.println("🔎 [DEPENDENCY CHECK] Searching for dependencies matching:");
            System.out.println("   └─ Report Type: " + latestJobEntity.getReportType());
            System.out.println("   └─ User Role: " + latestJobEntity.getUserRole());
            
            // Trigger dependent jobs
            List<String> triggeredJobIds = jobDependencyService.triggerDependentJobs(latestJobEntity, jwtToken);
            
            if (!triggeredJobIds.isEmpty()) {
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("✅ [DEPENDENT JOBS TRIGGERED] Parent Job: " + parentJobId);
                System.out.println("   Number of dependent jobs created: " + triggeredJobIds.size());
                for (int i = 0; i < triggeredJobIds.size(); i++) {
                    System.out.println("   " + (i + 1) + ". Dependent Job ID: " + triggeredJobIds.get(i));
                }
                System.out.println("   └─ All dependent jobs have been queued and will start processing");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            } else {
                System.out.println("ℹ️  [DEPENDENCY CHECK] No dependent jobs found for parent: " + parentJobId);
                System.out.println("   └─ No dependencies configured for this report type/role combination");
            }
        } catch (Exception e) {
            // Log error but don't fail the parent job
            System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.err.println("❌ [DEPENDENCY CHECK ERROR] Parent Job: " + parentJobId);
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   ⚠️  Parent job status not affected - error in dependency checking only");
            System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            e.printStackTrace();
        }
    }
    
    /**
     * Process data in chunks with streaming file generation to avoid memory exhaustion
     * Writes chunks directly to file instead of accumulating in memory
     */
    private String processDataInChunksStreaming(PipelineExtractionRequest request, ReportJobEntity job, String jwtToken) {
        System.out.println("🔄 BackgroundProcessingService: Processing data in chunks with streaming for job: " + job.getJobId());
        
        // Create reports directory if it doesn't exist
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        
        // Generate filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("report_%s_%s.%s", 
            job.getJobId(), 
            timestamp, 
            getFileExtension(job.getDataFormat())
        );
        String filePath = reportsDir.getAbsolutePath() + File.separator + filename;
        
        try {
            // First, get actual total record count from database
            long totalRecords = estimateTotalRecords(request);
            job.setTotalRecords(totalRecords);
            jobRepository.save(job);
            
            System.out.println("📊 Actual total records: " + totalRecords);
            
            // PDF format requires different handling - collect all data first, then generate PDF
            String format = job.getDataFormat() != null ? job.getDataFormat().toUpperCase() : "JSON";
            if ("PDF".equals(format)) {
                return processPdfGeneration(request, job, jwtToken, filePath, totalRecords);
            }
            
            // For JSON, CSV, XML - use streaming approach
            FileWriter writer = new FileWriter(filePath);
            boolean isFirstChunk = true;
            
            // Write file header based on format
            writeFileHeader(writer, job, isFirstChunk);
            
            // Process data in chunks
            int chunkSize = job.getChunkSize() != null ? job.getChunkSize() : 1000;
            int page = 0;
            long processedRecords = 0;
            int consecutiveEmptyChunks = 0;
            final int MAX_EMPTY_CHUNKS = 3; // Safety limit
            
            while (processedRecords < totalRecords && consecutiveEmptyChunks < MAX_EMPTY_CHUNKS) {
                // Check if job was cancelled
                Optional<ReportJobEntity> currentJob = jobRepository.findByJobId(job.getJobId());
                if (currentJob.isPresent() && "CANCELLED".equals(currentJob.get().getStatus())) {
                    System.out.println("⚠️ Job cancelled during processing: " + job.getJobId());
                    writer.close();
                    // Delete partial file
                    new File(filePath).delete();
                    throw new RuntimeException("Job was cancelled");
                }
                
                // Update progress every chunk
                jobQueueService.updateJobProgress(job.getJobId(), processedRecords, totalRecords);
                
                // Process chunk with retry
                List<MaskedTimesheetData> chunkData = processChunkWithRetry(request, page, chunkSize, jwtToken, job.getJobId());
                
                if (chunkData.isEmpty()) {
                    consecutiveEmptyChunks++;
                    System.out.println("⚠️ Empty chunk received (consecutive: " + consecutiveEmptyChunks + ")");
                } else {
                    consecutiveEmptyChunks = 0;
                    // Write chunk directly to file (don't accumulate in memory)
                    writeChunkToFile(writer, chunkData, job, isFirstChunk);
                    isFirstChunk = false;
                    
                    processedRecords += chunkData.size();
                    System.out.println("📊 Processed chunk: " + processedRecords + "/" + totalRecords + " records (page " + page + ")");
                }
                
                page++;
                
                // Break if no more data
                if (chunkData.size() < chunkSize) {
                    break;
                }
            }
            
            // Write file footer
            writeFileFooter(writer, job);
            writer.close();
            
            // Final progress update
            jobQueueService.updateJobProgress(job.getJobId(), processedRecords, totalRecords);
            
            System.out.println("✅ BackgroundProcessingService: Data processing completed - " + processedRecords + " records written to " + filePath);
            return filePath;
            
        } catch (Exception e) {
            System.err.println("❌ Error processing data in chunks: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process data in chunks", e);
        }
    }
    
    /**
     * Process PDF generation - collects all data first, then generates PDF
     * PDF cannot be streamed like JSON/CSV/XML, so we need to collect all chunks first
     */
    private String processPdfGeneration(PipelineExtractionRequest request, ReportJobEntity job, String jwtToken, 
                                       String filePath, long totalRecords) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token is required for PDF generation");
        }
        System.out.println("📄 BackgroundProcessingService: Processing PDF generation (collecting all data first)");
        
        try {
            List<MaskedTimesheetData> allData = new java.util.ArrayList<>();
            int chunkSize = job.getChunkSize() != null ? job.getChunkSize() : 1000;
            int page = 0;
            long processedRecords = 0;
            int consecutiveEmptyChunks = 0;
            final int MAX_EMPTY_CHUNKS = 3;
            
            // Collect all data in chunks
            while (processedRecords < totalRecords && consecutiveEmptyChunks < MAX_EMPTY_CHUNKS) {
                // Check if job was cancelled
                Optional<ReportJobEntity> currentJob = jobRepository.findByJobId(job.getJobId());
                if (currentJob.isPresent() && "CANCELLED".equals(currentJob.get().getStatus())) {
                    System.out.println("⚠️ Job cancelled during PDF processing: " + job.getJobId());
                    throw new RuntimeException("Job was cancelled");
                }
                
                // Update progress
                jobQueueService.updateJobProgress(job.getJobId(), processedRecords, totalRecords);
                
                // Process chunk with retry
                List<MaskedTimesheetData> chunkData = processChunkWithRetry(request, page, chunkSize, jwtToken, job.getJobId());
                
                if (chunkData.isEmpty()) {
                    consecutiveEmptyChunks++;
                    System.out.println("⚠️ Empty chunk received (consecutive: " + consecutiveEmptyChunks + ")");
                } else {
                    consecutiveEmptyChunks = 0;
                    // Collect data for PDF generation
                    allData.addAll(chunkData);
                    processedRecords += chunkData.size();
                    System.out.println("📊 PDF: Collected chunk - " + processedRecords + "/" + totalRecords + " records (page " + page + ")");
                }
                
                page++;
                
                // Break if no more data
                if (chunkData.size() < chunkSize) {
                    break;
                }
            }
            
            // Final progress update
            jobQueueService.updateJobProgress(job.getJobId(), processedRecords, totalRecords);
            
            // Generate PDF with all collected data
            System.out.println("📄 BackgroundProcessingService: Generating PDF with " + allData.size() + " records");
            generatePdfReport(allData, filePath, job, jwtToken);
            
            System.out.println("✅ BackgroundProcessingService: PDF generation completed - " + processedRecords + " records in " + filePath);
            return filePath;
            
        } catch (Exception e) {
            System.err.println("❌ Error processing PDF generation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process PDF generation", e);
        }
    }
    
    /**
     * Process chunk with retry mechanism
     */
    private List<MaskedTimesheetData> processChunkWithRetry(PipelineExtractionRequest request, int page, int chunkSize, String jwtToken, String jobId) {
        int maxRetries = 3;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                return processChunk(request, page, chunkSize, jwtToken);
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    System.err.println("❌ Failed to process chunk after " + maxRetries + " attempts: " + e.getMessage());
                    throw new RuntimeException("Chunk processing failed after retries", e);
                }
                System.out.println("⚠️ Chunk processing failed (attempt " + attempt + "/" + maxRetries + "), retrying...");
                try {
                    Thread.sleep(1000 * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Chunk processing interrupted", ie);
                }
            }
        }
        return new java.util.ArrayList<>();
    }
    
    /**
     * Process a single chunk of data with proper pagination
     * Uses JWT token for field masking and data access control
     */
    private List<MaskedTimesheetData> processChunk(PipelineExtractionRequest request, int page, int chunkSize, String jwtToken) {
        try {
            System.out.println("🔍 BackgroundProcessingService: Processing chunk - page: " + page + ", size: " + chunkSize);
            
            // Use ReportGenerationService to get data with pagination
            ReportGenerationRequest reportRequest = new ReportGenerationRequest();
            reportRequest.setUserRole(request.getUserRole());
            reportRequest.setReportType(request.getReportType());
            reportRequest.setUserCounty(request.getUserCounty());
            
            // Set pagination parameters
            reportRequest.setPage(page);
            reportRequest.setPageSize(chunkSize);
            
            // Set date range if available
            if (request.getDateRange() != null) {
                reportRequest.setStartDate(request.getDateRange().getStartDate());
                reportRequest.setEndDate(request.getDateRange().getEndDate());
            }
            
            // ALWAYS use JWT token for field masking and data access control
            // If no token, fail the job as we can't ensure proper security
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new IllegalArgumentException("JWT token is required for batch job processing to ensure proper field masking and data access control");
            }
            
            // Generate report with JWT token for proper field masking and permissions
            ReportGenerationResponse response = reportGenerationService.generateReport(reportRequest, jwtToken);
            
            if ("SUCCESS".equals(response.getStatus())) {
                // Convert ReportData to MaskedTimesheetData format
                List<MaskedTimesheetData> chunkData = convertToMaskedData(response.getData().getRecords());
                System.out.println("✅ BackgroundProcessingService: Chunk processed - " + chunkData.size() + " records (with field masking applied)");
                return chunkData;
            } else {
                System.err.println("❌ Chunk extraction failed: " + response.getErrorMessage());
                throw new RuntimeException("Chunk extraction failed: " + response.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing chunk: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process chunk", e);
        }
    }
    
    /**
     * Write file header based on format
     */
    private void writeFileHeader(FileWriter writer, ReportJobEntity job, boolean isFirstChunk) throws IOException {
        String format = job.getDataFormat().toUpperCase();
        
        switch (format) {
            case "JSON":
                writer.write("{\n");
                writer.write("  \"reportId\": \"" + job.getJobId() + "\",\n");
                writer.write("  \"reportType\": \"" + job.getReportType() + "\",\n");
                writer.write("  \"userRole\": \"" + job.getUserRole() + "\",\n");
                writer.write("  \"targetSystem\": \"" + job.getTargetSystem() + "\",\n");
                writer.write("  \"generatedAt\": \"" + LocalDateTime.now().toString() + "\",\n");
                writer.write("  \"dataFormat\": \"" + job.getDataFormat() + "\",\n");
                writer.write("  \"data\": [\n");
                break;
            case "CSV":
                writer.write("timesheetId,userRole,reportType,maskedAt,fields\n");
                break;
            case "XML":
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<report>\n");
                writer.write("  <metadata>\n");
                writer.write("    <reportId>" + job.getJobId() + "</reportId>\n");
                writer.write("    <reportType>" + job.getReportType() + "</reportType>\n");
                writer.write("    <userRole>" + job.getUserRole() + "</userRole>\n");
                writer.write("    <targetSystem>" + job.getTargetSystem() + "</targetSystem>\n");
                writer.write("    <generatedAt>" + LocalDateTime.now().toString() + "</generatedAt>\n");
                writer.write("  </metadata>\n");
                writer.write("  <data>\n");
                break;
            case "PDF":
                // PDF is handled separately, not streamed
                break;
            default:
                // Default to JSON format
                writer.write("{\n");
                writer.write("  \"reportId\": \"" + job.getJobId() + "\",\n");
                writer.write("  \"reportType\": \"" + job.getReportType() + "\",\n");
                writer.write("  \"userRole\": \"" + job.getUserRole() + "\",\n");
                writer.write("  \"targetSystem\": \"" + job.getTargetSystem() + "\",\n");
                writer.write("  \"generatedAt\": \"" + LocalDateTime.now().toString() + "\",\n");
                writer.write("  \"dataFormat\": \"" + job.getDataFormat() + "\",\n");
                writer.write("  \"data\": [\n");
        }
    }
    
    /**
     * Write chunk to file based on format
     */
    private void writeChunkToFile(FileWriter writer, List<MaskedTimesheetData> chunkData, ReportJobEntity job, boolean isFirstChunk) throws IOException {
        String format = job.getDataFormat().toUpperCase();
        
        switch (format) {
            case "JSON":
                for (int i = 0; i < chunkData.size(); i++) {
                    MaskedTimesheetData record = chunkData.get(i);
                    if (!isFirstChunk || i > 0) {
                        writer.write(",\n");
                    }
                    writer.write("    {\n");
                    writer.write("      \"timesheetId\": \"" + record.getTimesheetId() + "\",\n");
                    writer.write("      \"userRole\": \"" + record.getUserRole() + "\",\n");
                    writer.write("      \"reportType\": \"" + record.getReportType() + "\",\n");
                    writer.write("      \"maskedAt\": \"" + record.getMaskedAt() + "\",\n");
                    writer.write("      \"fields\": " + convertFieldsToJson(record.getFields()) + "\n");
                    writer.write("    }");
                }
                break;
            case "CSV":
                for (MaskedTimesheetData record : chunkData) {
                    writer.write(String.format("%s,%s,%s,%s,%s\n",
                        escapeCsv(record.getTimesheetId()),
                        escapeCsv(record.getUserRole()),
                        escapeCsv(record.getReportType()),
                        escapeCsv(record.getMaskedAt().toString()),
                        escapeCsv(convertFieldsToCsv(record.getFields()))
                    ));
                }
                break;
            case "XML":
                for (MaskedTimesheetData record : chunkData) {
                    writer.write("    <record>\n");
                    writer.write("      <timesheetId>" + escapeXml(record.getTimesheetId()) + "</timesheetId>\n");
                    writer.write("      <userRole>" + escapeXml(record.getUserRole()) + "</userRole>\n");
                    writer.write("      <reportType>" + escapeXml(record.getReportType()) + "</reportType>\n");
                    writer.write("      <maskedAt>" + escapeXml(record.getMaskedAt().toString()) + "</maskedAt>\n");
                    writer.write("      <fields>" + escapeXml(convertFieldsToXml(record.getFields())) + "</fields>\n");
                    writer.write("    </record>\n");
                }
                break;
            case "PDF":
                // PDF is handled separately, not streamed
                break;
        }
    }
    
    /**
     * Write file footer based on format
     */
    private void writeFileFooter(FileWriter writer, ReportJobEntity job) throws IOException {
        String format = job.getDataFormat().toUpperCase();
        
        switch (format) {
            case "JSON":
                writer.write("\n  ]\n");
                writer.write("}\n");
                break;
            case "CSV":
                // CSV doesn't need footer
                break;
            case "XML":
                writer.write("  </data>\n");
                writer.write("</report>\n");
                break;
            case "PDF":
                // PDF is handled separately
                break;
        }
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Escape XML special characters
     */
    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    /**
     * Generate JSON report
     */
    private void generateJsonReport(List<MaskedTimesheetData> data, String filePath, ReportJobEntity job) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("{\n");
            writer.write("  \"reportId\": \"" + job.getJobId() + "\",\n");
            writer.write("  \"reportType\": \"" + job.getReportType() + "\",\n");
            writer.write("  \"userRole\": \"" + job.getUserRole() + "\",\n");
            writer.write("  \"targetSystem\": \"" + job.getTargetSystem() + "\",\n");
            writer.write("  \"generatedAt\": \"" + LocalDateTime.now().toString() + "\",\n");
            writer.write("  \"totalRecords\": " + data.size() + ",\n");
            writer.write("  \"data\": [\n");
            
            for (int i = 0; i < data.size(); i++) {
                MaskedTimesheetData record = data.get(i);
                writer.write("    {\n");
                writer.write("      \"timesheetId\": \"" + record.getTimesheetId() + "\",\n");
                writer.write("      \"userRole\": \"" + record.getUserRole() + "\",\n");
                writer.write("      \"reportType\": \"" + record.getReportType() + "\",\n");
                writer.write("      \"maskedAt\": \"" + record.getMaskedAt() + "\",\n");
                writer.write("      \"fields\": " + convertFieldsToJson(record.getFields()) + "\n");
                writer.write("    }");
                if (i < data.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            
            writer.write("  ]\n");
            writer.write("}\n");
        }
    }
    
    /**
     * Generate CSV report
     */
    private void generateCsvReport(List<MaskedTimesheetData> data, String filePath, ReportJobEntity job) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write("timesheetId,userRole,reportType,maskedAt,fields\n");
            
            // Write data
            for (MaskedTimesheetData record : data) {
                writer.write(String.format("%s,%s,%s,%s,%s\n",
                    record.getTimesheetId(),
                    record.getUserRole(),
                    record.getReportType(),
                    record.getMaskedAt(),
                    convertFieldsToCsv(record.getFields())
                ));
            }
        }
    }
    
    /**
     * Generate PDF report
     */
    private void generatePdfReport(List<MaskedTimesheetData> data, String filePath, ReportJobEntity job, String jwtToken) throws IOException {
        try {
            System.out.println("📄 BackgroundProcessingService: Generating PDF report for job: " + job.getJobId());
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new IllegalArgumentException("JWT token is required for PDF generation");
            }
            
            // Convert MaskedTimesheetData to Map format for PDF generation
            List<Map<String, Object>> reportData = convertToMapList(data);
            
            // Prepare additional data
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("dateRange", "Generated for job: " + job.getJobId());
            additionalData.put("jobId", job.getJobId());
            additionalData.put("targetSystem", job.getTargetSystem());
            
            // Generate PDF using PDFReportGeneratorService with JWT token
            String pdfFilePath = pdfReportGeneratorService.generatePDFReportToFile(
                job.getReportType(),
                job.getUserRole(),
                reportData,
                additionalData,
                filePath,
                jwtToken
            );
            
            System.out.println("✅ BackgroundProcessingService: PDF report generated: " + pdfFilePath);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating PDF report: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to generate PDF report", e);
        }
    }
    
    /**
     * Generate XML report
     */
    private void generateXmlReport(List<MaskedTimesheetData> data, String filePath, ReportJobEntity job) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<report>\n");
            writer.write("  <metadata>\n");
            writer.write("    <reportId>" + job.getJobId() + "</reportId>\n");
            writer.write("    <reportType>" + job.getReportType() + "</reportType>\n");
            writer.write("    <userRole>" + job.getUserRole() + "</userRole>\n");
            writer.write("    <targetSystem>" + job.getTargetSystem() + "</targetSystem>\n");
            writer.write("    <generatedAt>" + LocalDateTime.now().toString() + "</generatedAt>\n");
            writer.write("    <totalRecords>" + data.size() + "</totalRecords>\n");
            writer.write("  </metadata>\n");
            writer.write("  <data>\n");
            
            for (MaskedTimesheetData record : data) {
                writer.write("    <record>\n");
                writer.write("      <timesheetId>" + record.getTimesheetId() + "</timesheetId>\n");
                writer.write("      <userRole>" + record.getUserRole() + "</userRole>\n");
                writer.write("      <reportType>" + record.getReportType() + "</reportType>\n");
                writer.write("      <maskedAt>" + record.getMaskedAt() + "</maskedAt>\n");
                writer.write("      <fields>" + convertFieldsToXml(record.getFields()) + "</fields>\n");
                writer.write("    </record>\n");
            }
            
            writer.write("  </data>\n");
            writer.write("</report>\n");
        }
    }
    
    // Helper methods
    /**
     * Create extraction request using JWT token-based user info (preferred) or fallback to request data
     */
    private PipelineExtractionRequest createExtractionRequest(ReportJobEntity job, BIReportRequest originalRequest,
                                                              String userRole, String countyId) {
        PipelineExtractionRequest request = new PipelineExtractionRequest();
        
        // Use role from token (source of truth for permissions)
        request.setUserRole(userRole != null ? userRole : job.getUserRole());
        request.setReportType(job.getReportType());
        
        // Use county from token (source of truth for data access)
        request.setUserCounty(countyId);
        
        System.out.println("🔍 BackgroundProcessingService: Creating extraction request - role: " + request.getUserRole() + 
                          ", county: " + countyId);
        
        // Set date range if available from request data
        if (originalRequest != null &&
            originalRequest.getStartDate() != null &&
            originalRequest.getEndDate() != null) {
            PipelineExtractionRequest.DateRange dateRange = new PipelineExtractionRequest.DateRange();
            dateRange.setStartDate(originalRequest.getStartDate());
            dateRange.setEndDate(originalRequest.getEndDate());
            request.setDateRange(dateRange);
        }
        
        System.out.println("✅ BackgroundProcessingService: Created extraction request - role: " + request.getUserRole() + 
                          ", county: " + request.getUserCounty());
        
        return request;
    }
    
    /**
     * Estimate total records using actual database count query
     * Uses JWT token-based permissions to determine what data can be accessed
     */
    private long estimateTotalRecords(PipelineExtractionRequest request) {
        try {
            System.out.println("🔍 BackgroundProcessingService: Estimating total records for job");
            System.out.println("🔍 BackgroundProcessingService: Request details - role: " + request.getUserRole() + 
                              ", county: " + request.getUserCounty());
            
            // Build query parameters based on user's permissions (from JWT token)
            QueryBuilderService.QueryParameters queryParams = queryBuilderService.buildQuery(
                request.getUserRole(),
                request.getUserCounty(),  // From JWT token
                request.getDateRange() != null ? request.getDateRange().getStartDate() : null,
                request.getDateRange() != null ? request.getDateRange().getEndDate() : null,
                null // additionalFilters
            );
            
            System.out.println("🔍 BackgroundProcessingService: Query params built - countyId: " + queryParams.getCountyId());
            
            // For COUNTY_WORKER, if countyId is null, we can't proceed
            // This should have been set from JWT token, but validate anyway
            if (UserRole.from(request.getUserRole()) == UserRole.CASE_WORKER &&
                (queryParams.getCountyId() == null || queryParams.getCountyId().trim().isEmpty())) {
                System.err.println("❌ Error: County filter is required for CASE_WORKER but was not provided in JWT token");
                throw new IllegalArgumentException("County filter is required for case workers. The JWT token should contain countyId information.");
            }
            
            // Get actual count from database (respects user's permissions)
            long totalCount = dataFetchingService.fetchData(queryParams, 0, 1).getTotalCount();
            
            System.out.println("✅ BackgroundProcessingService: Total records estimated: " + totalCount + " (based on user permissions)");
            return totalCount;
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error estimating total records: " + e.getMessage());
            System.err.println("❌ Error stack trace:");
            e.printStackTrace();
            // Fallback to a reasonable default if count query fails
            System.out.println("⚠️ BackgroundProcessingService: Using fallback count: 10000");
            return 10000;
        }
    }
    
    private String getFileExtension(String dataFormat) {
        switch (dataFormat.toUpperCase()) {
            case "JSON":
                return "json";
            case "CSV":
                return "csv";
            case "XML":
                return "xml";
            case "EXCEL":
                return "xlsx";
            case "PDF":
                return "pdf";
            default:
                return "json";
        }
    }
    
    private String convertFieldsToJson(java.util.Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    private String convertFieldsToCsv(java.util.Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        
        StringBuilder csv = new StringBuilder();
        boolean first = true;
        for (java.util.Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) {
                csv.append(";");
            }
            csv.append(entry.getKey()).append(":").append(entry.getValue());
            first = false;
        }
        return csv.toString();
    }
    
    private String convertFieldsToXml(java.util.Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        
        StringBuilder xml = new StringBuilder();
        for (java.util.Map.Entry<String, Object> entry : fields.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">");
            xml.append(entry.getValue());
            xml.append("</").append(entry.getKey()).append(">");
        }
        return xml.toString();
    }
    
    private List<MaskedTimesheetData> convertToMaskedData(List<java.util.Map<String, Object>> records) {
        List<MaskedTimesheetData> maskedData = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> record : records) {
            MaskedTimesheetData masked = new MaskedTimesheetData();
            masked.setTimesheetId(record.get("timesheetid") != null ? record.get("timesheetid").toString() : "");
            masked.setUserRole(record.get("userrole") != null ? record.get("userrole").toString() : "");
            masked.setReportType(record.get("reporttype") != null ? record.get("reporttype").toString() : "");
            masked.setMaskedAt(java.time.LocalDateTime.now());
            masked.setFields(record);
            maskedData.add(masked);
        }
        return maskedData;
    }
    
    /**
     * Convert MaskedTimesheetData to Map list for PDF generation
     */
    private List<Map<String, Object>> convertToMapList(List<MaskedTimesheetData> maskedData) {
        List<Map<String, Object>> mapList = new java.util.ArrayList<>();
        for (MaskedTimesheetData data : maskedData) {
            Map<String, Object> record = new HashMap<>();
            record.put("timesheetId", data.getTimesheetId());
            record.put("userRole", data.getUserRole());
            record.put("reportType", data.getReportType());
            record.put("maskedAt", data.getMaskedAt());
            
            // Add all fields from the masked data
            if (data.getFields() != null) {
                record.putAll(data.getFields());
            }
            
            mapList.add(record);
        }
        return mapList;
    }
}
