import { existsSync } from 'node:fs';
import { resolve } from 'node:path';

const ENV_FILE = resolve(process.cwd(), '.env');

if (existsSync(ENV_FILE)) {
  process.loadEnvFile(ENV_FILE);
}

let cachedApiUrl: string | null = null;

export function apiUrl(): string {
  if (cachedApiUrl !== null) {
    return cachedApiUrl;
  }
  const value = process.env['API_URL'];
  if (!value) {
    throw new Error('API_URL is not set. Add it to app/web/.env or export it before starting.');
  }
  if (!URL.canParse(value)) {
    throw new Error(`API_URL is not a valid URL: ${value}`);
  }
  cachedApiUrl = value.replace(/\/+$/, '');
  return cachedApiUrl;
}
