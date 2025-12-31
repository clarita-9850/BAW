-- Migration: Create persons table for IHSS CMIPS case creation
-- This table stores person/recipient information required for case creation

CREATE TABLE IF NOT EXISTS persons (
    person_id BIGSERIAL PRIMARY KEY,
    
    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    suffix VARCHAR(20),
    ssn VARCHAR(11), -- Format: XXX-XX-XXXX (stored encrypted/masked)
    date_of_birth DATE NOT NULL,
    gender VARCHAR(50),
    ethnicity VARCHAR(100),
    
    -- Contact Information
    preferred_spoken_language VARCHAR(50),
    preferred_written_language VARCHAR(50),
    primary_phone VARCHAR(20),
    secondary_phone VARCHAR(20),
    email VARCHAR(255),
    
    -- Residence Address
    residence_address_line1 VARCHAR(255) NOT NULL,
    residence_address_line2 VARCHAR(255),
    residence_city VARCHAR(100) NOT NULL,
    residence_state VARCHAR(2) NOT NULL DEFAULT 'CA',
    residence_zip VARCHAR(10) NOT NULL,
    
    -- Mailing Address (if different from residence)
    mailing_address_line1 VARCHAR(255),
    mailing_address_line2 VARCHAR(255),
    mailing_city VARCHAR(100),
    mailing_state VARCHAR(2),
    mailing_zip VARCHAR(10),
    mailing_same_as_residence BOOLEAN DEFAULT TRUE,
    
    -- Location Information
    county_of_residence VARCHAR(100) NOT NULL,
    
    -- Guardian/Conservator Information (optional)
    guardian_conservator_name VARCHAR(255),
    guardian_conservator_address VARCHAR(500),
    guardian_conservator_phone VARCHAR(20),
    
    -- Disaster Preparedness
    disaster_preparedness_code VARCHAR(3), -- 3-letter code (e.g., AAB)
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- Indexes for search performance
    CONSTRAINT unique_ssn UNIQUE (ssn)
);

-- Create indexes for common search queries
CREATE INDEX IF NOT EXISTS idx_persons_name ON persons(last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_persons_ssn ON persons(ssn) WHERE ssn IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_persons_dob ON persons(date_of_birth);
CREATE INDEX IF NOT EXISTS idx_persons_county ON persons(county_of_residence);
CREATE INDEX IF NOT EXISTS idx_persons_created_at ON persons(created_at);

-- Add comment to table
COMMENT ON TABLE persons IS 'Stores person/recipient information for IHSS CMIPS case creation';
COMMENT ON COLUMN persons.ssn IS 'Social Security Number - should be encrypted/masked in application layer';
COMMENT ON COLUMN persons.disaster_preparedness_code IS '3-letter code: First letter (contact degree), Second (impairment), Third (life support)';

