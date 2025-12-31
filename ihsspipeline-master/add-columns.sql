-- Add missing columns to support home services and domestic works data
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS provider_name VARCHAR(255);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS provider_email VARCHAR(255);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS provider_department VARCHAR(255);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS recipient_name VARCHAR(255);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS recipient_email VARCHAR(255);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS project_name VARCHAR(255);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS project_budget DECIMAL(10,2);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS hourly_rate DECIMAL(8,2);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS total_amount DECIMAL(10,2);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP;
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS approval_comments TEXT;

-- Add location-based columns for county access control
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS provider_county VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS recipient_county VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS service_location VARCHAR(255);

-- Add service-specific columns for home services
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS service_type VARCHAR(100);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS service_category VARCHAR(100);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS priority_level VARCHAR(20);
