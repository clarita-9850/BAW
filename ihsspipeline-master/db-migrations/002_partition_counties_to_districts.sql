-- Migration: Partition counties to their canonical districts
-- This ensures each county appears in only ONE district according to the canonical partition:
-- North:   Alameda, San Francisco, Santa Clara
-- Central: Orange, Los Angeles
-- South:   Riverside, San Diego

BEGIN;

-- Step 1: Move counties to their canonical districts
-- North District: Alameda, San Francisco, Santa Clara
UPDATE timesheets 
SET district_id = 'district-north'
WHERE provider_county IN ('Alameda', 'San Francisco', 'Santa Clara')
  AND district_id != 'district-north';

-- Central District: Orange, Los Angeles
UPDATE timesheets 
SET district_id = 'district-central'
WHERE provider_county IN ('Orange', 'Los Angeles')
  AND district_id != 'district-central';

-- South District: Riverside, San Diego
UPDATE timesheets 
SET district_id = 'district-south'
WHERE provider_county IN ('Riverside', 'San Diego')
  AND district_id != 'district-south';

-- Step 2: Verify the partition
-- Each county should now appear in only one district
DO $$
DECLARE
    county_record RECORD;
    district_count INTEGER;
BEGIN
    FOR county_record IN 
        SELECT DISTINCT provider_county 
        FROM timesheets 
        WHERE provider_county IS NOT NULL
    LOOP
        SELECT COUNT(DISTINCT district_id) INTO district_count
        FROM timesheets
        WHERE provider_county = county_record.provider_county
          AND district_id IS NOT NULL;
        
        IF district_count > 1 THEN
            RAISE WARNING 'County % appears in % districts after migration', 
                county_record.provider_county, district_count;
        END IF;
    END LOOP;
END $$;

COMMIT;

-- Verification query (run after migration)
-- Should show each county in exactly one district
SELECT 
    provider_county,
    COUNT(DISTINCT district_id) as district_count,
    STRING_AGG(DISTINCT district_id, ', ') as districts
FROM timesheets
WHERE provider_county IS NOT NULL 
  AND district_id IS NOT NULL
GROUP BY provider_county
ORDER BY provider_county;

