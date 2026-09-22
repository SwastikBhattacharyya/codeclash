SELECT 'CREATE DATABASE contest'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'contest')\gexec
