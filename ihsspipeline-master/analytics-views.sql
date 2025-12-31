-- =====================================================
-- Analytics Views for Tableau Real-Time Dashboards
-- =====================================================
-- These views optimize data for Tableau consumption
-- and provide pre-aggregated metrics for fast queries

-- =====================================================
-- View 1: Comprehensive Timesheet Analytics
-- =====================================================
CREATE OR REPLACE VIEW vw_timesheet_analytics AS
SELECT 
    timesheet_id,
    provider_id,
    provider_name,
    provider_email,
    provider_county,
    provider_department,
    -- Provider demographic fields
    provider_gender,
    provider_date_of_birth,
    EXTRACT(YEAR FROM AGE(provider_date_of_birth)) AS provider_age,
    CASE 
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 25 AND 34 THEN '25-34'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 35 AND 44 THEN '35-44'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 45 AND 54 THEN '45-54'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 55 AND 64 THEN '55-64'
        ELSE '65+'
    END AS provider_age_group,
    provider_ethnicity,
    recipient_id,
    recipient_name,
    recipient_county,
    -- Recipient demographic fields
    recipient_gender,
    recipient_date_of_birth,
    EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) AS recipient_age,
    CASE 
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 30 AND 39 THEN '30-39'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 40 AND 49 THEN '40-49'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 50 AND 59 THEN '50-59'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 60 AND 69 THEN '60-69'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 70 AND 79 THEN '70-79'
        ELSE '80+'
    END AS recipient_age_group,
    recipient_ethnicity,
    district_id,
    district_name,
    project_county,
    service_type,
    service_category,
    priority_level,
    status,
    total_hours,
    total_amount,
    hourly_rate,
    start_date,
    end_date,
    week_ending,
    submitted_at,
    approved_at,
    created_at,
    updated_at,
    -- Calculated fields for analytics
    CASE 
        WHEN status = 'APPROVED' THEN total_amount
        ELSE 0
    END AS approved_amount,
    CASE 
        WHEN status IN ('SUBMITTED', 'PENDING_REVIEW') THEN 1
        ELSE 0
    END AS pending_count,
    CASE 
        WHEN status = 'APPROVED' AND submitted_at IS NOT NULL AND approved_at IS NOT NULL 
        THEN EXTRACT(EPOCH FROM (approved_at - submitted_at))/3600
        ELSE NULL
    END AS approval_hours,
    CASE 
        WHEN status = 'APPROVED' THEN 1
        ELSE 0
    END AS approved_count,
    CASE 
        WHEN status = 'REJECTED' THEN 1
        ELSE 0
    END AS rejected_count,
    -- Date dimensions for time-based analysis
    DATE(created_at) AS created_date,
    DATE(submitted_at) AS submitted_date,
    DATE(approved_at) AS approved_date,
    EXTRACT(YEAR FROM created_at) AS created_year,
    EXTRACT(QUARTER FROM created_at) AS created_quarter,
    EXTRACT(MONTH FROM created_at) AS created_month,
    EXTRACT(WEEK FROM created_at) AS created_week,
    EXTRACT(DOW FROM created_at) AS created_day_of_week,
    -- Time dimensions
    EXTRACT(HOUR FROM created_at) AS created_hour
FROM timesheets;

-- =====================================================
-- View 2: Daily Aggregates
-- =====================================================
CREATE OR REPLACE VIEW vw_daily_analytics AS
SELECT 
    DATE(created_at) AS date,
    district_id,
    district_name,
    provider_county,
    recipient_county,
    service_type,
    -- Counts
    COUNT(*) AS total_timesheets,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) AS approved_count,
    COUNT(CASE WHEN status = 'PENDING_REVIEW' THEN 1 END) AS pending_count,
    COUNT(CASE WHEN status = 'SUBMITTED' THEN 1 END) AS submitted_count,
    COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) AS rejected_count,
    COUNT(CASE WHEN status = 'REVISION_REQUIRED' THEN 1 END) AS revision_required_count,
    -- Hours
    SUM(total_hours) AS total_hours,
    SUM(CASE WHEN status = 'APPROVED' THEN total_hours ELSE 0 END) AS approved_hours,
    AVG(total_hours) AS avg_hours_per_timesheet,
    -- Amounts
    SUM(total_amount) AS total_amount,
    SUM(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS approved_amount,
    AVG(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS avg_approved_amount,
    -- Rates
    CASE 
        WHEN COUNT(*) > 0 
        THEN COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(*)
        ELSE 0
    END AS approval_rate,
    -- Time metrics
    AVG(CASE 
        WHEN status = 'APPROVED' AND submitted_at IS NOT NULL AND approved_at IS NOT NULL 
        THEN EXTRACT(EPOCH FROM (approved_at - submitted_at))/3600
        ELSE NULL
    END) AS avg_approval_hours
FROM timesheets
GROUP BY DATE(created_at), district_id, district_name, provider_county, recipient_county, service_type;

-- =====================================================
-- View 3: Provider Performance Analytics
-- =====================================================
CREATE OR REPLACE VIEW vw_provider_analytics AS
SELECT 
    provider_id,
    provider_name,
    provider_email,
    provider_county,
    provider_department,
    -- Provider demographics
    provider_gender,
    EXTRACT(YEAR FROM AGE(provider_date_of_birth)) AS provider_age,
    CASE 
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 25 AND 34 THEN '25-34'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 35 AND 44 THEN '35-44'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 45 AND 54 THEN '45-54'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 55 AND 64 THEN '55-64'
        ELSE '65+'
    END AS provider_age_group,
    provider_ethnicity,
    district_id,
    district_name,
    -- Counts
    COUNT(*) AS total_timesheets,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) AS approved_timesheets,
    COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) AS rejected_timesheets,
    COUNT(CASE WHEN status = 'PENDING_REVIEW' THEN 1 END) AS pending_timesheets,
    COUNT(CASE WHEN status = 'REVISION_REQUIRED' THEN 1 END) AS revision_required_timesheets,
    -- Hours
    SUM(total_hours) AS total_hours,
    SUM(CASE WHEN status = 'APPROVED' THEN total_hours ELSE 0 END) AS approved_hours,
    AVG(total_hours) AS avg_hours_per_timesheet,
    -- Amounts
    SUM(total_amount) AS total_amount,
    SUM(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS total_earnings,
    AVG(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS avg_earnings_per_timesheet,
    -- Rates
    CASE 
        WHEN COUNT(*) > 0 
        THEN COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(*)
        ELSE 0
    END AS approval_rate,
    -- Dates
    MIN(created_at) AS first_timesheet_date,
    MAX(created_at) AS last_timesheet_date,
    MAX(approved_at) AS last_approval_date,
    -- Time metrics
    AVG(CASE 
        WHEN status = 'APPROVED' AND submitted_at IS NOT NULL AND approved_at IS NOT NULL 
        THEN EXTRACT(EPOCH FROM (approved_at - submitted_at))/3600)
        ELSE NULL
    END) AS avg_approval_time_hours
FROM timesheets
WHERE provider_id IS NOT NULL
GROUP BY provider_id, provider_name, provider_email, provider_county, provider_department, 
         provider_gender, EXTRACT(YEAR FROM AGE(provider_date_of_birth)), 
         CASE 
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 25 AND 34 THEN '25-34'
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 35 AND 44 THEN '35-44'
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 45 AND 54 THEN '45-54'
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 55 AND 64 THEN '55-64'
             ELSE '65+'
         END, provider_ethnicity, district_id, district_name;

-- =====================================================
-- View 4: District/County Performance Analytics
-- =====================================================
CREATE OR REPLACE VIEW vw_district_analytics AS
SELECT 
    district_id,
    district_name,
    provider_county,
    -- Counts
    COUNT(*) AS total_timesheets,
    COUNT(DISTINCT provider_id) AS unique_providers,
    COUNT(DISTINCT recipient_id) AS unique_recipients,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) AS approved_count,
    COUNT(CASE WHEN status = 'PENDING_REVIEW' THEN 1 END) AS pending_count,
    COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) AS rejected_count,
    -- Hours
    SUM(total_hours) AS total_hours,
    SUM(CASE WHEN status = 'APPROVED' THEN total_hours ELSE 0 END) AS approved_hours,
    AVG(total_hours) AS avg_hours_per_timesheet,
    -- Amounts
    SUM(total_amount) AS total_amount,
    SUM(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS total_approved_amount,
    AVG(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS avg_approved_amount,
    -- Rates
    CASE 
        WHEN COUNT(*) > 0 
        THEN COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(*)
        ELSE 0
    END AS approval_rate,
    -- Time metrics
    AVG(CASE 
        WHEN status = 'APPROVED' AND submitted_at IS NOT NULL AND approved_at IS NOT NULL 
        THEN EXTRACT(EPOCH FROM (approved_at - submitted_at))/3600
        ELSE NULL
    END) AS avg_approval_time_hours
FROM timesheets
WHERE district_id IS NOT NULL
GROUP BY district_id, district_name, provider_county;

-- =====================================================
-- View 5: Status Trends Over Time
-- =====================================================
CREATE OR REPLACE VIEW vw_status_trends AS
SELECT 
    DATE(created_at) AS date,
    status,
    district_id,
    district_name,
    provider_county,
    -- Counts
    COUNT(*) AS count,
    -- Hours
    SUM(total_hours) AS total_hours,
    AVG(total_hours) AS avg_hours,
    -- Amounts
    SUM(total_amount) AS total_amount,
    AVG(total_amount) AS avg_amount,
    -- Time dimensions
    EXTRACT(YEAR FROM created_at) AS year,
    EXTRACT(QUARTER FROM created_at) AS quarter,
    EXTRACT(MONTH FROM created_at) AS month,
    EXTRACT(WEEK FROM created_at) AS week,
    EXTRACT(DOW FROM created_at) AS day_of_week
FROM timesheets
GROUP BY DATE(created_at), status, district_id, district_name, provider_county, 
         EXTRACT(YEAR FROM created_at), EXTRACT(QUARTER FROM created_at), 
         EXTRACT(MONTH FROM created_at), EXTRACT(WEEK FROM created_at), EXTRACT(DOW FROM created_at);

-- =====================================================
-- View 6: Service Type Analytics
-- =====================================================
CREATE OR REPLACE VIEW vw_service_type_analytics AS
SELECT 
    service_type,
    service_category,
    district_id,
    district_name,
    provider_county,
    -- Counts
    COUNT(*) AS total_timesheets,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) AS approved_count,
    -- Hours
    SUM(total_hours) AS total_hours,
    AVG(total_hours) AS avg_hours,
    -- Amounts
    SUM(total_amount) AS total_amount,
    SUM(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS approved_amount,
    AVG(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS avg_approved_amount,
    -- Rates
    CASE 
        WHEN COUNT(*) > 0 
        THEN COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(*)
        ELSE 0
    END AS approval_rate
FROM timesheets
WHERE service_type IS NOT NULL
GROUP BY service_type, service_category, district_id, district_name, provider_county;

-- =====================================================
-- View 7: Demographic Analytics
-- =====================================================
CREATE OR REPLACE VIEW vw_demographic_analytics AS
SELECT 
    -- Provider demographics
    provider_gender,
    CASE 
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 25 AND 34 THEN '25-34'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 35 AND 44 THEN '35-44'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 45 AND 54 THEN '45-54'
        WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 55 AND 64 THEN '55-64'
        ELSE '65+'
    END AS provider_age_group,
    provider_ethnicity,
    -- Recipient demographics
    recipient_gender,
    CASE 
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 30 AND 39 THEN '30-39'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 40 AND 49 THEN '40-49'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 50 AND 59 THEN '50-59'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 60 AND 69 THEN '60-69'
        WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 70 AND 79 THEN '70-79'
        ELSE '80+'
    END AS recipient_age_group,
    recipient_ethnicity,
    -- Geographic
    district_id,
    district_name,
    provider_county,
    -- Metrics
    COUNT(*) AS total_timesheets,
    COUNT(DISTINCT provider_id) AS unique_providers,
    COUNT(DISTINCT recipient_id) AS unique_recipients,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) AS approved_count,
    SUM(total_hours) AS total_hours,
    SUM(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS approved_amount,
    AVG(total_hours) AS avg_hours,
    AVG(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS avg_approved_amount,
    CASE 
        WHEN COUNT(*) > 0 
        THEN COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(*)
        ELSE 0
    END AS approval_rate
FROM timesheets
WHERE provider_gender IS NOT NULL OR recipient_gender IS NOT NULL
GROUP BY provider_gender, 
         CASE 
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 25 AND 34 THEN '25-34'
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 35 AND 44 THEN '35-44'
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 45 AND 54 THEN '45-54'
             WHEN EXTRACT(YEAR FROM AGE(provider_date_of_birth)) BETWEEN 55 AND 64 THEN '55-64'
             ELSE '65+'
         END, 
         provider_ethnicity,
         recipient_gender,
         CASE 
             WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 30 AND 39 THEN '30-39'
             WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 40 AND 49 THEN '40-49'
             WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 50 AND 59 THEN '50-59'
             WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 60 AND 69 THEN '60-69'
             WHEN EXTRACT(YEAR FROM AGE(recipient_date_of_birth)) BETWEEN 70 AND 79 THEN '70-79'
             ELSE '80+'
         END,
         recipient_ethnicity,
         district_id, district_name, provider_county;

-- =====================================================
-- View 8: Real-Time Metrics (Last 24 Hours)
-- =====================================================
CREATE OR REPLACE VIEW vw_realtime_metrics AS
SELECT 
    'timesheets_submitted_today' AS metric_name,
    COUNT(*) AS metric_value,
    MAX(created_at) AS last_updated
FROM timesheets
WHERE DATE(created_at) = CURRENT_DATE
UNION ALL
SELECT 
    'timesheets_approved_today' AS metric_name,
    COUNT(*) AS metric_value,
    MAX(approved_at) AS last_updated
FROM timesheets
WHERE DATE(approved_at) = CURRENT_DATE AND status = 'APPROVED'
UNION ALL
SELECT 
    'pending_approvals' AS metric_name,
    COUNT(*) AS metric_value,
    MAX(created_at) AS last_updated
FROM timesheets
WHERE status IN ('SUBMITTED', 'PENDING_REVIEW')
UNION ALL
SELECT 
    'total_hours_this_week' AS metric_name,
    COALESCE(SUM(total_hours), 0) AS metric_value,
    MAX(created_at) AS last_updated
FROM timesheets
WHERE EXTRACT(WEEK FROM created_at) = EXTRACT(WEEK FROM CURRENT_DATE)
  AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
UNION ALL
SELECT 
    'total_approved_amount_today' AS metric_name,
    COALESCE(SUM(total_amount), 0) AS metric_value,
    MAX(approved_at) AS last_updated
FROM timesheets
WHERE DATE(approved_at) = CURRENT_DATE AND status = 'APPROVED';

-- =====================================================
-- Performance Indexes for Analytics Views
-- =====================================================

-- Indexes for fast Tableau queries
CREATE INDEX IF NOT EXISTS idx_timesheets_created_at ON timesheets(created_at);
CREATE INDEX IF NOT EXISTS idx_timesheets_status ON timesheets(status);
CREATE INDEX IF NOT EXISTS idx_timesheets_district_county ON timesheets(district_id, provider_county);
CREATE INDEX IF NOT EXISTS idx_timesheets_dates ON timesheets(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_timesheets_provider ON timesheets(provider_id, status);
CREATE INDEX IF NOT EXISTS idx_timesheets_submitted_at ON timesheets(submitted_at);
CREATE INDEX IF NOT EXISTS idx_timesheets_approved_at ON timesheets(approved_at);
CREATE INDEX IF NOT EXISTS idx_timesheets_service_type ON timesheets(service_type, status);
CREATE INDEX IF NOT EXISTS idx_timesheets_composite ON timesheets(district_id, provider_county, status, created_at);
CREATE INDEX IF NOT EXISTS idx_timesheets_provider_demographics ON timesheets(provider_gender, provider_ethnicity);
CREATE INDEX IF NOT EXISTS idx_timesheets_recipient_demographics ON timesheets(recipient_gender, recipient_ethnicity);
CREATE INDEX IF NOT EXISTS idx_timesheets_provider_dob ON timesheets(provider_date_of_birth);
CREATE INDEX IF NOT EXISTS idx_timesheets_recipient_dob ON timesheets(recipient_date_of_birth);

-- =====================================================
-- Materialized View for Dashboard Performance
-- =====================================================
-- Uncomment and customize if you need even better performance

/*
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_dashboard_summary AS
SELECT 
    DATE(created_at) AS date,
    district_id,
    district_name,
    provider_county,
    COUNT(*) AS total_timesheets,
    SUM(total_hours) AS total_hours,
    SUM(CASE WHEN status = 'APPROVED' THEN total_amount ELSE 0 END) AS approved_amount,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) AS approved_count
FROM timesheets
WHERE created_at >= CURRENT_DATE - INTERVAL '90 days'
GROUP BY DATE(created_at), district_id, district_name, provider_county;

-- Create unique index for concurrent refresh
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_dashboard_summary 
ON mv_dashboard_summary(date, district_id, provider_county);

-- Function to refresh materialized view
CREATE OR REPLACE FUNCTION refresh_dashboard_summary()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_dashboard_summary;
END;
$$ LANGUAGE plpgsql;
*/

-- =====================================================
-- Grant Permissions (if using separate analytics user)
-- =====================================================
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO tableau_user;
-- GRANT SELECT ON ALL VIEWS IN SCHEMA public TO tableau_user;

