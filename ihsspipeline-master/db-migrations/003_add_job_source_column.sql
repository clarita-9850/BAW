-- Add job_source column to report_jobs table
-- This column distinguishes between SCHEDULED, MANUAL, and API jobs

ALTER TABLE report_jobs 
ADD COLUMN IF NOT EXISTS job_source VARCHAR(50) DEFAULT 'MANUAL';

-- Update existing jobs to have MANUAL as default
UPDATE report_jobs 
SET job_source = 'MANUAL' 
WHERE job_source IS NULL;

-- Add comment
COMMENT ON COLUMN report_jobs.job_source IS 'Source of the job: SCHEDULED (cron jobs), MANUAL (dashboard), API (programmatic)';

-- Verify
SELECT job_source, COUNT(*) as count 
FROM report_jobs 
GROUP BY job_source;

