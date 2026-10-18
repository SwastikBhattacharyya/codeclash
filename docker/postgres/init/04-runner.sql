SELECT 'CREATE DATABASE runner'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'runner')\gexec
