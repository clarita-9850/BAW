DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cmips_app') THEN
        CREATE ROLE cmips_app WITH LOGIN PASSWORD 'cmips_app_password';
    END IF;
END
$$;

ALTER ROLE cmips_app WITH LOGIN PASSWORD 'cmips_app_password';

ALTER DATABASE ihsscmips OWNER TO cmips_app;

GRANT ALL PRIVILEGES ON DATABASE ihsscmips TO cmips_app;

