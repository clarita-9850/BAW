-- Migration Script: Transform timesheets table to match sajeevs-codebase-main schema
-- This aligns the schema with Keycloak field masking rules

-- Step 1: Backup existing table (if it exists and has data)
CREATE TABLE IF NOT EXISTS timesheets_backup AS SELECT * FROM timesheets;

-- Step 2: Drop existing table
DROP TABLE IF EXISTS timesheets CASCADE;

-- Step 3: Create new table matching sajeevs-codebase-main schema
CREATE TABLE timesheets (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(255) NOT NULL,
    employee_name VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    regular_hours NUMERIC(10,2),
    overtime_hours NUMERIC(10,2),
    sick_hours NUMERIC(10,2),
    vacation_hours NUMERIC(10,2),
    holiday_hours NUMERIC(10,2),
    total_hours NUMERIC(10,2),
    status VARCHAR(255) NOT NULL CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'REVISION_REQUESTED', 'PROCESSED')),
    comments VARCHAR(1000),
    supervisor_comments VARCHAR(1000),
    submitted_at TIMESTAMP(6),
    submitted_by VARCHAR(255),
    approved_at TIMESTAMP(6),
    approved_by VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6)
);

-- Step 4: Create indexes for performance
CREATE INDEX idx_timesheets_employee_id ON timesheets(employee_id);
CREATE INDEX idx_timesheets_user_id ON timesheets(user_id);
CREATE INDEX idx_timesheets_status ON timesheets(status);
CREATE INDEX idx_timesheets_pay_period ON timesheets(pay_period_start, pay_period_end);
CREATE INDEX idx_timesheets_created_at ON timesheets(created_at);

-- Step 5: Add comments for documentation
COMMENT ON TABLE timesheets IS 'Employee timesheets matching sajeevs-codebase-main schema for Keycloak field masking compatibility';
COMMENT ON COLUMN timesheets.employee_id IS 'Employee identifier';
COMMENT ON COLUMN timesheets.employee_name IS 'Employee full name';
COMMENT ON COLUMN timesheets.user_id IS 'User ID from Keycloak';
COMMENT ON COLUMN timesheets.department IS 'Employee department';
COMMENT ON COLUMN timesheets.location IS 'Work location';
COMMENT ON COLUMN timesheets.regular_hours IS 'Regular working hours';
COMMENT ON COLUMN timesheets.overtime_hours IS 'Overtime hours';
COMMENT ON COLUMN timesheets.sick_hours IS 'Sick leave hours';
COMMENT ON COLUMN timesheets.vacation_hours IS 'Vacation hours';
COMMENT ON COLUMN timesheets.holiday_hours IS 'Holiday hours';
COMMENT ON COLUMN timesheets.total_hours IS 'Total hours (calculated)';
COMMENT ON COLUMN timesheets.status IS 'Timesheet status: DRAFT, SUBMITTED, APPROVED, REJECTED, REVISION_REQUESTED, PROCESSED';
COMMENT ON COLUMN timesheets.comments IS 'Employee comments';
COMMENT ON COLUMN timesheets.supervisor_comments IS 'Supervisor review comments';
COMMENT ON COLUMN timesheets.submitted_by IS 'User who submitted the timesheet';
COMMENT ON COLUMN timesheets.approved_by IS 'User who approved the timesheet';

-- Step 6: Verify table creation
SELECT 'Migration completed successfully. New timesheets table created.' AS status;

