-- Clear all batch jobs data from report_jobs table
-- This script removes all existing batch job records to start fresh

-- Option 1: Delete all jobs (recommended for cleanup)
DELETE FROM report_jobs;

-- Option 2: Delete only completed/failed/cancelled jobs (keep active jobs)
-- DELETE FROM report_jobs WHERE status IN ('COMPLETED', 'FAILED', 'CANCELLED');

-- Option 3: Delete jobs older than 30 days
-- DELETE FROM report_jobs WHERE created_at < NOW() - INTERVAL '30 days';

-- Reset sequence if using auto-increment (not applicable for UUID-based job_id)
-- ALTER SEQUENCE report_jobs_job_id_seq RESTART WITH 1;

-- Verify deletion
SELECT COUNT(*) as remaining_jobs FROM report_jobs;

