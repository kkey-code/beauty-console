#!/usr/bin/env bash
set -Eeuo pipefail

release_dir="${1:?usage: update-server.sh RELEASE_DIR [DEPLOY_DIR]}"
deploy_dir="${2:-/home/ubuntu/beauty-console-deploy}"
compose_file="${deploy_dir}/docker-compose.server.yml"
env_file="${deploy_dir}/.env"
timestamp="$(date +%Y%m%d-%H%M%S)"
backup_dir="${deploy_dir}/backups/${timestamp}"
rollback_app="beauty-console-app:rollback-${timestamp}"
rollback_web="beauty-console-web:rollback-${timestamp}"
update_started=0

if [[ "${EUID}" -ne 0 ]]; then
  echo "Please run this script with sudo."
  exit 1
fi

for required_file in "${release_dir}/manifest.sha256" "${release_dir}/images.tar" "${release_dir}/docker-compose.server.yml" "${env_file}"; do
  if [[ ! -f "${required_file}" ]]; then
    echo "Missing required file: ${required_file}"
    exit 1
  fi
done

cd "${release_dir}"
sha256sum --check manifest.sha256

set -a
# shellcheck disable=SC1090
source "${env_file}"
set +a

compose=(docker compose --env-file "${env_file}" -f "${compose_file}")
mkdir -p "${backup_dir}"
chmod 700 "${deploy_dir}/backups" "${backup_dir}"

rollback() {
  exit_code=$?
  if [[ "${exit_code}" -eq 0 ]]; then
    return
  fi

  echo "Update failed. Restoring the previous application images..."
  if docker image inspect "${rollback_app}" >/dev/null 2>&1; then
    docker tag "${rollback_app}" beauty-console-app:latest
  fi
  if docker image inspect "${rollback_web}" >/dev/null 2>&1; then
    docker tag "${rollback_web}" beauty-console-web:latest
  fi
  if [[ -f "${backup_dir}/docker-compose.server.yml" ]]; then
    cp "${backup_dir}/docker-compose.server.yml" "${compose_file}"
  fi
  if [[ "${update_started}" -eq 1 ]]; then
    "${compose[@]}" up -d --no-deps --force-recreate app || true
    "${compose[@]}" up -d --no-deps --force-recreate web || true
  fi
  echo "Database backup: ${backup_dir}/database.sql.gz"
  echo "Inspect the error before restoring the database backup."
  exit "${exit_code}"
}
trap rollback ERR

mysql_id="$("${compose[@]}" ps -q mysql)"
if [[ -z "${mysql_id}" ]]; then
  echo "MySQL container is not running."
  exit 1
fi

echo "Creating database backup..."
docker exec -e MYSQL_PWD="${STORE_DB_PASSWORD}" "${mysql_id}" \
  mysqldump --single-transaction --routines --triggers --no-tablespaces \
  --default-character-set=utf8mb4 --user="${STORE_DB_USERNAME}" "${STORE_DB_DATABASE}" \
  | gzip -9 > "${backup_dir}/database.sql.gz"
chmod 600 "${backup_dir}/database.sql.gz"

cp "${compose_file}" "${backup_dir}/docker-compose.server.yml"
if docker image inspect beauty-console-app:latest >/dev/null 2>&1; then
  docker tag beauty-console-app:latest "${rollback_app}"
fi
if docker image inspect beauty-console-web:latest >/dev/null 2>&1; then
  docker tag beauty-console-web:latest "${rollback_web}"
fi

echo "Loading application images..."
docker load --input "${release_dir}/images.tar"

run_mysql() {
  docker exec -i -e MYSQL_PWD="${STORE_DB_PASSWORD}" "${mysql_id}" \
    mysql --default-character-set=utf8mb4 --user="${STORE_DB_USERNAME}" "${STORE_DB_DATABASE}" "$@"
}

run_mysql --execute="
CREATE TABLE IF NOT EXISTS schema_migration (
  migration_id VARCHAR(190) NOT NULL PRIMARY KEY,
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
"

if [[ -d "${release_dir}/migrations" ]]; then
  while IFS= read -r migration; do
    migration_id="$(basename "${migration}")"
    applied="$(run_mysql --batch --skip-column-names --execute="SELECT COUNT(*) FROM schema_migration WHERE migration_id='${migration_id//\'/\'\'}';")"
    if [[ "${applied}" == "0" ]]; then
      echo "Applying migration: ${migration_id}"
      run_mysql < "${migration}"
      run_mysql --execute="INSERT INTO schema_migration(migration_id) VALUES ('${migration_id//\'/\'\'}');"
    else
      echo "Skipping applied migration: ${migration_id}"
    fi
  done < <(find "${release_dir}/migrations" -maxdepth 1 -type f -name '*.sql' | sort)
fi

cp "${release_dir}/docker-compose.server.yml" "${compose_file}"
chmod 600 "${compose_file}"
update_started=1

wait_for_health() {
  service_name="$1"
  for _ in $(seq 1 36); do
    container_id="$("${compose[@]}" ps -q "${service_name}")"
    if [[ -n "${container_id}" ]]; then
      health="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${container_id}")"
      if [[ "${health}" == "healthy" || "${health}" == "running" ]]; then
        return 0
      fi
      if [[ "${health}" == "unhealthy" || "${health}" == "exited" || "${health}" == "dead" ]]; then
        docker logs --tail 100 "${container_id}" || true
        return 1
      fi
    fi
    sleep 5
  done
  echo "Timed out waiting for ${service_name}."
  return 1
}

echo "Restarting backend..."
"${compose[@]}" up -d --no-deps --force-recreate app
wait_for_health app

echo "Restarting frontend..."
"${compose[@]}" up -d --no-deps --force-recreate web
wait_for_health web
curl --fail --silent --show-error --max-time 10 http://127.0.0.1/ >/dev/null

trap - ERR
docker image rm "${rollback_app}" "${rollback_web}" >/dev/null 2>&1 || true
echo "Update completed successfully."
"${compose[@]}" ps
echo "Database backup: ${backup_dir}/database.sql.gz"
