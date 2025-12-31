-- Remove project_name, project_budget, and project_id columns from timesheets table
ALTER TABLE timesheets DROP COLUMN IF EXISTS project_name;
ALTER TABLE timesheets DROP COLUMN IF EXISTS project_budget;
ALTER TABLE timesheets DROP COLUMN IF EXISTS project_id;
