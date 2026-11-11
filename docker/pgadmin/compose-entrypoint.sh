#!/bin/sh
set -euf

servers=/var/lib/pgadmin/servers.json
pgpass=/var/lib/pgadmin/pgpass

pgpass_escape() {
  printf '%s' "$1" | sed 's/[\\:]/\\&/g'
}

json_escape() {
  printf '%s' "$1" | sed 's/[\\"]/\\&/g'
}

printf '%s:%s:*:%s:%s\n' db 5432 "$(pgpass_escape "$DB_USER")" "$(pgpass_escape "$DB_PASSWORD")" >"$pgpass"
chmod 600 "$pgpass"

user=$(json_escape "$DB_USER")
{
  printf '{"Servers": {'
  index=0
  for database in $(printf '%s' "$DB_DATABASES" | tr ',' ' '); do
    index=$((index + 1))
    [ "$index" -gt 1 ] && printf ','
    name=$(json_escape "$database")
    printf '"%d": {"Name": "%s", "Group": "CodeClash", "Host": "db", "Port": 5432, "MaintenanceDB": "%s", "Username": "%s", "SSLMode": "prefer", "PassFile": "%s"}' \
      "$index" "$name" "$name" "$user" "$pgpass"
  done
  printf '}}\n'
} >"$servers"

export PGADMIN_SERVER_JSON_FILE="$servers"
exec /entrypoint.sh
