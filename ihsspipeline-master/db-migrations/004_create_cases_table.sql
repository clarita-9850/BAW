-- Migration: Create cases table for IHSS CMIPS case management
-- This table stores case information linked to persons/recipients

CREATE TABLE IF NOT EXISTS cases (
    case_id BIGSERIAL PRIMARY KEY,
    
    -- Foreign Key to Person
    person_id BIGINT NOT NULL REFERENCES persons(person_id) ON DELETE RESTRICT,
    
    -- Case Numbers
    cmips_case_number VARCHAR(7) UNIQUE NOT NULL, -- 7-digit CMIPS case number
    legacy_case_number VARCHAR(10), -- 10-digit legacy case number (county code + 8 digits)
    
    -- Case Status
    case_status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- ACTIVE, INACTIVE, CLOSED, PENDING
    
    -- Location Information
    county_code VARCHAR(10) NOT NULL,
    district_id VARCHAR(50),
    district_name VARCHAR(100),
    
    -- Assignment
    assigned_caseworker_id VARCHAR(100) NOT NULL,
    
    -- Dates
    case_opened_date DATE,
    case_closed_date DATE,
    
    -- Additional Case Information
    case_notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT valid_case_status CHECK (case_status IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'PENDING')),
    CONSTRAINT valid_dates CHECK (case_closed_date IS NULL OR case_opened_date IS NULL OR case_closed_date >= case_opened_date)
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_cases_person_id ON cases(person_id);
CREATE INDEX IF NOT EXISTS idx_cases_cmips_number ON cases(cmips_case_number);
CREATE INDEX IF NOT EXISTS idx_cases_legacy_number ON cases(legacy_case_number);
CREATE INDEX IF NOT EXISTS idx_cases_caseworker ON cases(assigned_caseworker_id);
CREATE INDEX IF NOT EXISTS idx_cases_status ON cases(case_status);
CREATE INDEX IF NOT EXISTS idx_cases_county ON cases(county_code);
CREATE INDEX IF NOT EXISTS idx_cases_opened_date ON cases(case_opened_date);
CREATE INDEX IF NOT EXISTS idx_cases_created_at ON cases(created_at);

-- Add comments
COMMENT ON TABLE cases IS 'Stores IHSS CMIPS case information linked to persons/recipients';
COMMENT ON COLUMN cases.cmips_case_number IS '7-digit CMIPS case number assigned upon case creation';
COMMENT ON COLUMN cases.legacy_case_number IS '10-digit legacy case number: 2-digit county code + 8-digit case number';
COMMENT ON COLUMN cases.disaster_preparedness_code IS '3-letter code: First letter (contact degree), Second (impairment), Third (life support)';

