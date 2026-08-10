SELECT 'CREATE DATABASE problem'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'problem')\gexec
