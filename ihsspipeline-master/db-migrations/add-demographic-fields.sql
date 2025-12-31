-- Migration: Add Demographic Fields to Timesheets Table
-- Date: 2025-11-29
-- Description: Adds provider and recipient demographic fields for gender, ethnicity, age group, and date of birth

-- Add Provider Demographic Fields
ALTER TABLE timesheets 
ADD COLUMN IF NOT EXISTS provider_gender VARCHAR(50),
ADD COLUMN IF NOT EXISTS provider_ethnicity VARCHAR(100),
ADD COLUMN IF NOT EXISTS provider_age_group VARCHAR(50),
ADD COLUMN IF NOT EXISTS provider_date_of_birth DATE;

-- Add Recipient Demographic Fields
ALTER TABLE timesheets 
ADD COLUMN IF NOT EXISTS recipient_gender VARCHAR(50),
ADD COLUMN IF NOT EXISTS recipient_ethnicity VARCHAR(100),
ADD COLUMN IF NOT EXISTS recipient_age_group VARCHAR(50),
ADD COLUMN IF NOT EXISTS recipient_date_of_birth DATE;

-- Add Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_timesheets_provider_gender ON timesheets(provider_gender) WHERE provider_gender IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_timesheets_provider_ethnicity ON timesheets(provider_ethnicity) WHERE provider_ethnicity IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_timesheets_provider_age_group ON timesheets(provider_age_group) WHERE provider_age_group IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_timesheets_recipient_gender ON timesheets(recipient_gender) WHERE recipient_gender IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_timesheets_recipient_ethnicity ON timesheets(recipient_ethnicity) WHERE recipient_ethnicity IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_timesheets_recipient_age_group ON timesheets(recipient_age_group) WHERE recipient_age_group IS NOT NULL;

-- Add Comments for Documentation
COMMENT ON COLUMN timesheets.provider_gender IS 'Provider gender: Male, Female, Non-Binary, Prefer Not to Say, Other';
COMMENT ON COLUMN timesheets.provider_ethnicity IS 'Provider ethnicity: Hispanic/Latino, White, Black/African American, Asian, Native American, Pacific Islander, Other';
COMMENT ON COLUMN timesheets.provider_age_group IS 'Provider age group: 18-24, 25-34, 35-44, 45-54, 55-64, 65+';
COMMENT ON COLUMN timesheets.provider_date_of_birth IS 'Provider date of birth for age calculation';
COMMENT ON COLUMN timesheets.recipient_gender IS 'Recipient gender: Male, Female, Non-Binary, Prefer Not to Say, Other';
COMMENT ON COLUMN timesheets.recipient_ethnicity IS 'Recipient ethnicity: Hispanic/Latino, White, Black/African American, Asian, Native American, Pacific Islander, Other';
COMMENT ON COLUMN timesheets.recipient_age_group IS 'Recipient age group: 18-24, 25-34, 35-44, 45-54, 55-64, 65+';
COMMENT ON COLUMN timesheets.recipient_date_of_birth IS 'Recipient date of birth for age calculation';

-- Verification Query
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'timesheets' 
AND column_name IN (
    'provider_gender', 'provider_ethnicity', 'provider_age_group', 'provider_date_of_birth',
    'recipient_gender', 'recipient_ethnicity', 'recipient_age_group', 'recipient_date_of_birth'
)
ORDER BY column_name;

