-- Normalize legacy district IDs and system county names to canonical values
-- Canonical districts: district-north, district-central, district-south
-- Canonical counties: Orange, Los Angeles, Alameda, Riverside, San Francisco, Santa Clara, San Diego

BEGIN;

-- 1) Normalize legacy district IDs to canonical IDs
UPDATE timesheets
SET district_id = 'district-north'
WHERE district_id IN ('DIST001', 'DIST002');

UPDATE timesheets
SET district_id = 'district-central'
WHERE district_id IN ('DIST003', 'DIST004');

UPDATE timesheets
SET district_id = 'district-south'
WHERE district_id IN ('DIST005', 'DIST006');

-- 2) Normalize legacy system county names to canonical real counties
--    Partition chosen:
--      North:   Alameda, San Francisco, Santa Clara
--      Central: Orange, Los Angeles
--      South:   Riverside, San Diego

UPDATE timesheets
SET provider_county = 'Alameda'
WHERE provider_county = 'NORTH_COUNTY';

UPDATE timesheets
SET provider_county = 'Orange'
WHERE provider_county = 'CENTRAL_COUNTY';

UPDATE timesheets
SET provider_county = 'Riverside'
WHERE provider_county = 'SOUTH_COUNTY';

-- 3) Partition counties to their canonical districts
--    This ensures each county appears in only ONE district
--    North:   Alameda, San Francisco, Santa Clara
--    Central: Orange, Los Angeles
--    South:   Riverside, San Diego

UPDATE timesheets 
SET district_id = 'district-north'
WHERE provider_county IN ('Alameda', 'San Francisco', 'Santa Clara')
  AND district_id != 'district-north';

UPDATE timesheets 
SET district_id = 'district-central'
WHERE provider_county IN ('Orange', 'Los Angeles')
  AND district_id != 'district-central';

UPDATE timesheets 
SET district_id = 'district-south'
WHERE provider_county IN ('Riverside', 'San Diego')
  AND district_id != 'district-south';

COMMIT;


