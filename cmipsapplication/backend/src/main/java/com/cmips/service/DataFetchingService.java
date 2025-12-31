package com.cmips.service;

import com.cmips.entity.Timesheet;
import com.cmips.entity.TimesheetStatus;
import com.cmips.model.UserRole;
import com.cmips.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Data Fetching Service
 * Executes optimized database queries to fetch only the authorized data based on constructed query parameters
 */
@Service
public class DataFetchingService {

    public DataFetchingService() {
        System.out.println("🔧 DataFetchingService: Constructor called - initializing...");
        System.out.println("✅ DataFetchingService: Constructor completed successfully");
    }

    @Autowired
    private TimesheetRepository timesheetRepository;
    
    @Autowired
    private CountyMappingService countyMappingService;

    /**
     * Fetches data based on query parameters with pagination support
     */
    public DataFetchResult fetchData(QueryBuilderService.QueryParameters queryParams) {
        return fetchData(queryParams, 0, 500);
    }
    
    /**
     * Fetches data based on query parameters with pagination
     */
    public DataFetchResult fetchData(QueryBuilderService.QueryParameters queryParams, int page, int pageSize) {
        System.out.println("🔍 DataFetchingService: Fetching data for role: " + queryParams.getUserRole());
        System.out.println("🔍 DataFetchingService: CountyId in queryParams: " + queryParams.getCountyId());
        System.out.println("🔍 DataFetchingService: CountyId is null: " + (queryParams.getCountyId() == null));
        System.out.println("🔍 DataFetchingService: CountyId is empty: " + (queryParams.getCountyId() != null && queryParams.getCountyId().trim().isEmpty()));
        
        try {
            List<Timesheet> rawData;
            long totalCount;
            
            UserRole role = UserRole.from(queryParams.getUserRole());
            System.out.println("🔍 DataFetchingService: Parsed UserRole enum: " + role);
            switch (role) {
                case ADMIN, SYSTEM_SCHEDULER -> {
                    if (queryParams.getCountyId() != null && !queryParams.getCountyId().trim().isEmpty()) {
                        System.out.println("🔍 DataFetchingService: " + role + " with county filter: " + queryParams.getCountyId());
                        rawData = fetchCountyData(queryParams, page, pageSize);
                        totalCount = getTotalCountForCountyData(queryParams);
                    } else {
                        rawData = fetchAllData(queryParams, page, pageSize);
                        totalCount = getTotalCountForAllData(queryParams);
                    }
                }
                case SUPERVISOR -> {
                    // SUPERVISOR can access all data, county filter is optional
                    String countyId = queryParams.getCountyId();
                    if (countyId != null && !countyId.trim().isEmpty()) {
                        System.out.println("🔍 DataFetchingService: SUPERVISOR with optional county filter: " + countyId);
                        rawData = fetchCountyData(queryParams, page, pageSize);
                        totalCount = getTotalCountForCountyData(queryParams);
                    } else {
                        System.out.println("🔍 DataFetchingService: SUPERVISOR without county filter - fetching all data");
                        rawData = fetchAllData(queryParams, page, pageSize);
                        totalCount = getTotalCountForAllData(queryParams);
                    }
                }
                case CASE_WORKER -> {
                    // CASE_WORKER must have a county filter
                    String countyId = queryParams.getCountyId();
                    if (countyId == null || countyId.trim().isEmpty()) {
                        throw new IllegalArgumentException("Location filter is required for case workers. Please select a county from the dropdown.");
                    }
                    System.out.println("🔍 DataFetchingService: CASE_WORKER with county filter: " + countyId);
                    rawData = fetchCountyData(queryParams, page, pageSize);
                    totalCount = getTotalCountForCountyData(queryParams);
                }
                case PROVIDER, RECIPIENT -> {
                    rawData = fetchOwnRecordsData(queryParams, page, pageSize);
                    totalCount = getTotalCountForOwnRecordsData(queryParams);
                }
                default -> throw new IllegalArgumentException("Unsupported user role: " + queryParams.getUserRole());
            }
            
            System.out.println("✅ DataFetchingService: Fetched " + rawData.size() + " records (page " + page + "), total count: " + totalCount);
            return new DataFetchResult(true, "Data fetched successfully", rawData, rawData.size(), totalCount);
            
        } catch (Exception e) {
            System.err.println("❌ DataFetchingService: Error fetching data: " + e.getMessage());
            e.printStackTrace();
            return new DataFetchResult(false, "Failed to fetch data: " + e.getMessage(), null, 0, 0);
        }
    }

    /**
     * Fetches all data for Central Worker with pagination
     */
    private List<Timesheet> fetchAllData(QueryBuilderService.QueryParameters queryParams, int page, int pageSize) {
        System.out.println("🔍 Fetching all data for Central Worker (page: " + page + ", size: " + pageSize + ")");
        
        if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
            int offset = page * pageSize;
            System.out.println("⚠️ Using date range query with pagination (page: " + page + ", size: " + pageSize + ", offset: " + offset + ")");
            return timesheetRepository.findByDateRangeWithPagination(
                queryParams.getStartDate(), queryParams.getEndDate(), offset, pageSize
            );
        } else {
            int offset = page * pageSize;
            System.out.println("⚠️ Using pagination for most recent records (page: " + page + ", size: " + pageSize + ", offset: " + offset + ")");
            return timesheetRepository.findMostRecentWithPagination(offset, pageSize);
        }
    }

    /**
     * Fetches location-specific data for County Workers with pagination.
     */
    private List<Timesheet> fetchCountyData(QueryBuilderService.QueryParameters queryParams, int page, int pageSize) {
        System.out.println("🔍 DataFetchingService: Fetching location data (page: " + page + ", size: " + pageSize + ")");

        String locationFilter = queryParams.getCountyId();
        if (locationFilter == null || locationFilter.trim().isEmpty()) {
            throw new IllegalArgumentException("Location filter is required for county workers");
        }

        int offset = page * pageSize;

        if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
            return timesheetRepository.findByLocationAndDateRangeWithPagination(
                    locationFilter,
                    queryParams.getStartDate(),
                    queryParams.getEndDate(),
                    offset,
                    pageSize
            );
        }

        return timesheetRepository.findByLocationWithPagination(locationFilter, offset, pageSize);
    }

    /**
     * Fetches own records data for Providers and Recipients with pagination
     */
    private List<Timesheet> fetchOwnRecordsData(QueryBuilderService.QueryParameters queryParams, int page, int pageSize) {
        System.out.println("🔍 Fetching own records data for: " + queryParams.getUserRole() + " (page: " + page + ", size: " + pageSize + ")");
        
        String userId = queryParams.getUserId();
        
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required for providers/recipients");
        }
        
        if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
            int offset = page * pageSize;
            System.out.println("⚠️ Using database pagination for date range query (page: " + page + ", size: " + pageSize + ", offset: " + offset + ")");
            return timesheetRepository.findByUserAndDateRangeWithPagination(
                userId, queryParams.getStartDate(), queryParams.getEndDate(), offset, pageSize
            );
        } else {
            int offset = page * pageSize;
            System.out.println("⚠️ Using database pagination for user query (page: " + page + ", size: " + pageSize + ", offset: " + offset + ")");
            return timesheetRepository.findByUserWithPagination(userId, offset, pageSize);
        }
    }

    /**
     * Fetches data with custom filters
     */
    public DataFetchResult fetchDataWithCustomFilters(QueryBuilderService.QueryParameters queryParams, 
                                                     Map<String, Object> customFilters) {
        System.out.println("🔍 DataFetchingService: Fetching data with custom filters");
        
        try {
            List<Timesheet> rawData;
            
            // Apply custom filters based on query parameters
            if (customFilters.containsKey("status")) {
                String status = (String) customFilters.get("status");
                if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
                    System.out.println("⚠️ Using date range query with safety limit of 5,000 records to prevent memory issues.");
                    rawData = timesheetRepository.findByDateRangeWithLimit(
                        queryParams.getStartDate(), queryParams.getEndDate(), 5000
                    );
                    rawData = rawData.stream()
                        .filter(entity -> status.equals(entity.getStatus().name()))
                        .collect(java.util.stream.Collectors.toList());
                } else {
                    rawData = timesheetRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                        .limit(5000)
                        .collect(java.util.stream.Collectors.toList());
                }
            } else if (customFilters.containsKey("serviceType") || customFilters.containsKey("department")) {
                String filterValue = (String) customFilters.getOrDefault("serviceType", customFilters.get("department"));
                if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
                    System.out.println("⚠️ Using date range query with safety limit of 5,000 records to prevent memory issues.");
                    rawData = timesheetRepository.findByDateRangeWithLimit(
                        queryParams.getStartDate(), queryParams.getEndDate(), 5000
                    );
                    rawData = rawData.stream()
                        .filter(entity -> filterValue.equals(entity.getDepartment()))
                        .collect(java.util.stream.Collectors.toList());
                } else {
                    System.out.println("⚠️ WARNING: No date range specified. Limiting to most recent 5,000 records to prevent memory issues.");
                    rawData = timesheetRepository.findMostRecentWithLimit(5000).stream()
                        .filter(entity -> filterValue.equals(entity.getDepartment()))
                        .collect(java.util.stream.Collectors.toList());
                }
            } else {
                return fetchData(queryParams);
            }
            
            System.out.println("✅ DataFetchingService: Fetched " + rawData.size() + " records with custom filters");
            return new DataFetchResult(true, "Data fetched successfully with custom filters", rawData, rawData.size());
            
        } catch (Exception e) {
            System.err.println("❌ DataFetchingService: Error fetching data with custom filters: " + e.getMessage());
            e.printStackTrace();
            return new DataFetchResult(false, "Failed to fetch data with custom filters: " + e.getMessage(), null, 0);
        }
    }

    /**
     * Gets data statistics for the fetched records
     */
    public DataStatistics getDataStatistics(List<Timesheet> data) {
        if (data == null || data.isEmpty()) {
            return new DataStatistics(0, 0.0, 0.0, 0);
        }
        
        int totalRecords = data.size();
        double totalHours = data.stream()
            .mapToDouble(entity -> entity.getTotalHours() != null ? entity.getTotalHours().doubleValue() : 0.0)
            .sum();
        double totalAmount = 0.0;
        int uniqueEmployees = (int) data.stream().map(Timesheet::getEmployeeId).distinct().count();
        
        return new DataStatistics(totalRecords, totalHours, totalAmount, uniqueEmployees);
    }

    /**
     * Get total count for all data (Central Worker)
     */
    private long getTotalCountForAllData(QueryBuilderService.QueryParameters queryParams) {
        if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
            return timesheetRepository.countByDateRange(queryParams.getStartDate(), queryParams.getEndDate());
        } else {
            return timesheetRepository.countMostRecent();
        }
    }
    
    /**
     * Get total count for location data
     */
    private long getTotalCountForCountyData(QueryBuilderService.QueryParameters queryParams) {
        String locationFilter = queryParams.getCountyId();

        if (locationFilter == null || locationFilter.trim().isEmpty()) {
            throw new IllegalArgumentException("Location filter is required for geography-restricted roles");
        }

        boolean hasDateRange = queryParams.getStartDate() != null && queryParams.getEndDate() != null;

        if (hasDateRange) {
            return timesheetRepository.countByLocationAndDateRange(
                locationFilter, queryParams.getStartDate(), queryParams.getEndDate()
            );
        }
        return timesheetRepository.countByLocation(locationFilter);
    }
    
    /**
     * Get total count for own records data (Provider/Recipient)
     */
    private long getTotalCountForOwnRecordsData(QueryBuilderService.QueryParameters queryParams) {
        String userId = queryParams.getUserId();
        
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required for providers/recipients");
        }
        
        boolean hasDateRange = queryParams.getStartDate() != null && queryParams.getEndDate() != null;
        
        if (hasDateRange) {
            return timesheetRepository.countByUserAndDateRange(
                userId, queryParams.getStartDate(), queryParams.getEndDate()
            );
        }
        return timesheetRepository.countByUser(userId);
    }

    public static class DataFetchResult {
        private final boolean success;
        private final String message;
        private final List<Timesheet> data;
        private final int recordCount;
        private final long totalCount;

        public DataFetchResult(boolean success, String message, List<Timesheet> data, int recordCount) {
            this(success, message, data, recordCount, recordCount);
        }
        
        public DataFetchResult(boolean success, String message, List<Timesheet> data, int recordCount, long totalCount) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.recordCount = recordCount;
            this.totalCount = totalCount;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<Timesheet> getData() { return data; }
        public int getRecordCount() { return recordCount; }
        public long getTotalCount() { return totalCount; }
    }

    public static class DataStatistics {
        private final int totalRecords;
        private final double totalHours;
        private final double totalAmount;
        private final int uniqueProviders;

        public DataStatistics(int totalRecords, double totalHours, double totalAmount, int uniqueProviders) {
            this.totalRecords = totalRecords;
            this.totalHours = totalHours;
            this.totalAmount = totalAmount;
            this.uniqueProviders = uniqueProviders;
        }

        public int getTotalRecords() { return totalRecords; }
        public double getTotalHours() { return totalHours; }
        public double getTotalAmount() { return totalAmount; }
        public int getUniqueProviders() { return uniqueProviders; }
    }
}






