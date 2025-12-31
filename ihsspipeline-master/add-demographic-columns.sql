-- Add demographic columns to timesheets table for analytics dashboard
-- This script adds gender, date of birth, and ethnicity fields for both providers and recipients

-- Add provider demographic columns
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS provider_gender VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS provider_date_of_birth DATE;
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS provider_ethnicity VARCHAR(100);

-- Add recipient demographic columns
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS recipient_gender VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS recipient_date_of_birth DATE;
ALTER TABLE timesheets ADD COLUMN IF NOT EXISTS recipient_ethnicity VARCHAR(100);

-- Verify columns were added
SELECT 
    column_name, 
    data_type, 
    character_maximum_length
FROM information_schema.columns
WHERE table_name = 'timesheets' 
  AND column_name IN (
    'provider_gender', 
    'provider_date_of_birth', 
    'provider_ethnicity',
    'recipient_gender',
    'recipient_date_of_birth',
    'recipient_ethnicity'
  )
ORDER BY column_name;

