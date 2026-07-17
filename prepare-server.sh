#!/usr/bin/env bash
set -euo pipefail

if [[ -f .env ]]; then
  echo ".env 已存在，未覆盖。"
else
  umask 077
  mysql_root_password="$(openssl rand -hex 24)"
  store_db_password="$(openssl rand -hex 24)"
  store_redis_password="$(openssl rand -hex 24)"
  jwt_secret="$(openssl rand -hex 48)"

  cat > .env <<EOF
MYSQL_ROOT_PASSWORD=${mysql_root_password}
STORE_DB_DATABASE=db_platform
STORE_DB_USERNAME=store_app
STORE_DB_PASSWORD=${store_db_password}
STORE_REDIS_PASSWORD=${store_redis_password}
STORE_REDIS_DATABASE=0
STORE_JWT_ADMIN_SECRET_KEY=${jwt_secret}
STORE_JWT_ADMIN_TTL=7200000
STORE_API_DOCS_ENABLED=true
EOF
  chmod 600 .env
  echo "已生成仅当前用户可读的 .env。"
fi

if [[ -z "$(swapon --show=NAME --noheadings)" ]]; then
  if [[ ! -f /swapfile ]]; then
    sudo fallocate -l 2G /swapfile
  fi
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile >/dev/null
  sudo swapon /swapfile
  if ! grep -q '^/swapfile none swap sw 0 0$' /etc/fstab; then
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab >/dev/null
  fi
fi

echo "服务器初始化完成。"
free -h
