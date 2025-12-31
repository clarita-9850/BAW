-- Test Data Generation Script for CMIPS Application
-- Generates test data for timesheets to test analytics, field masking, and report generation

-- Function to generate random dates within a range
CREATE OR REPLACE FUNCTION random_date(start_date DATE, end_date DATE) 
RETURNS DATE AS $$
BEGIN
    RETURN start_date + (random() * (end_date - start_date))::INTEGER;
END;
$$ LANGUAGE plpgsql;

-- Clear existing test data (optional - uncomment if needed)
-- TRUNCATE TABLE timesheets CASCADE;

-- Generate test data
DO $$
DECLARE
    -- Arrays for realistic data generation
    departments TEXT[] := ARRAY['Home Care Services', 'Personal Care', 'Domestic Services', 'Medical Services', 'Rehabilitation Services'];
    locations TEXT[] := ARRAY['Los Angeles', 'Orange', 'Riverside', 'San Francisco', 'Alameda', 'Santa Clara', 'San Diego'];
    statuses TEXT[] := ARRAY['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED'];
    
    -- First and last names
    first_names TEXT[] := ARRAY['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emily', 'Robert', 'Jessica', 'James', 'Amanda', 'William', 'Ashley', 'Richard', 'Melissa', 'Joseph', 'Michelle', 'Thomas', 'Kimberly', 'Charles', 'Amy'];
    last_names TEXT[] := ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin', 'Lee'];
    
    -- Variables
    i INTEGER;
    user_id TEXT;
    employee_id TEXT;
    employee_name TEXT;
    dept TEXT;
    loc TEXT;
    start_dt DATE;
    end_dt DATE;
    reg_hrs NUMERIC(10,2);
    ot_hrs NUMERIC(10,2);
    total_hrs NUMERIC(10,2);
    ts_status TEXT;
    comments_text TEXT;
    created_dt TIMESTAMP;
    submitted_dt TIMESTAMP;
    approved_dt TIMESTAMP;
    
BEGIN
    -- Generate 500 records
    FOR i IN 1..500 LOOP
        -- Generate unique IDs
        user_id := 'user-' || LPAD((i % 50 + 1)::TEXT, 4, '0');
        employee_id := 'emp-' || LPAD((i % 100 + 1)::TEXT, 4, '0');
        employee_name := first_names[(i % 20) + 1] || ' ' || last_names[(i % 20) + 1];
        
        -- Select department and location
        dept := departments[(i % 5) + 1];
        loc := locations[(i % 7) + 1];
        
        -- Generate date range (pay period)
        start_dt := random_date('2024-01-01'::DATE, '2024-12-31'::DATE);
        end_dt := start_dt + INTERVAL '13 days'; -- 2-week pay period
        
        -- Generate hours
        reg_hrs := (random() * 40 + 20)::NUMERIC(10,2); -- 20-60 hours
        ot_hrs := CASE WHEN random() > 0.7 THEN (random() * 10)::NUMERIC(10,2) ELSE 0 END; -- 30% chance of overtime
        total_hrs := reg_hrs + ot_hrs;
        
        -- Select status with distribution
        ts_status := CASE 
            WHEN (i % 10) < 2 THEN 'DRAFT'
            WHEN (i % 10) < 5 THEN 'SUBMITTED'
            WHEN (i % 10) < 9 THEN 'APPROVED'
            ELSE 'REJECTED'
        END;
        
        -- Generate timestamps
        created_dt := NOW() - (random() * 90)::INTEGER * INTERVAL '1 day';
        submitted_dt := CASE WHEN ts_status IN ('SUBMITTED', 'APPROVED', 'REJECTED') 
            THEN created_dt + (random() * 2)::INTEGER * INTERVAL '1 day' 
            ELSE NULL END;
        approved_dt := CASE WHEN ts_status = 'APPROVED' 
            THEN submitted_dt + (random() * 5)::INTEGER * INTERVAL '1 day' 
            ELSE NULL END;
        
        -- Generate comments
        comments_text := CASE 
            WHEN ts_status = 'APPROVED' THEN 'All services completed as scheduled. Client satisfied.'
            WHEN ts_status = 'SUBMITTED' THEN 'Awaiting supervisor review and approval.'
            WHEN ts_status = 'REJECTED' THEN 'Timesheet rejected. Please review and resubmit with corrections.'
            ELSE 'Timesheet in draft status.'
        END;
        
        -- Insert record
        INSERT INTO timesheets (
            user_id, employee_id, employee_name, department, location,
            pay_period_start, pay_period_end,
            regular_hours, overtime_hours, total_hours,
            status, comments, submitted_by, submitted_at,
            approved_by, approved_at, created_at, updated_at
        ) VALUES (
            user_id, employee_id, employee_name, dept, loc,
            start_dt, end_dt,
            reg_hrs, ot_hrs, total_hrs,
            ts_status, comments_text, 
            CASE WHEN submitted_dt IS NOT NULL THEN user_id ELSE NULL END,
            submitted_dt,
            CASE WHEN approved_dt IS NOT NULL THEN 'supervisor1' ELSE NULL END,
            approved_dt,
            created_dt, created_dt
        );
        
        -- Progress indicator every 100 records
        IF i % 100 = 0 THEN
            RAISE NOTICE 'Generated % records...', i;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Test data generation complete! Generated 500 records.';
END $$;

-- Verify data generation
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(DISTINCT employee_id) as unique_employees,
    COUNT(DISTINCT location) as unique_locations,
    COUNT(DISTINCT department) as unique_departments
FROM timesheets;

-- Summary by status
SELECT 
    status,
    COUNT(*) as count,
    ROUND(AVG(total_hours)::NUMERIC, 2) as avg_hours,
    ROUND(SUM(total_hours)::NUMERIC, 2) as total_hours
FROM timesheets
GROUP BY status
ORDER BY count DESC;

-- Summary by location
SELECT 
    location,
    COUNT(*) as total_timesheets,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved,
    ROUND(SUM(total_hours)::NUMERIC, 2) as total_hours
FROM timesheets
GROUP BY location
ORDER BY location;

-- Summary by department
SELECT 
    department,
    COUNT(*) as total_timesheets,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved,
    ROUND(AVG(total_hours)::NUMERIC, 2) as avg_hours
FROM timesheets
GROUP BY department
ORDER BY department;

-- Recent timesheets (last 30 days)
SELECT 
    COUNT(*) as recent_timesheets,
    COUNT(CASE WHEN status = 'SUBMITTED' THEN 1 END) as pending_approvals,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_today
FROM timesheets
WHERE created_at >= NOW() - INTERVAL '30 days';

-- Clean up function
DROP FUNCTION IF EXISTS random_date(DATE, DATE);

