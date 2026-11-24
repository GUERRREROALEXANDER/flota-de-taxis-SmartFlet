#!/bin/bash
# Script que EC2 ejecuta UNA vez al crear la instancia (Amazon Linux 2023).
# crear-en-aws.ps1 reemplaza __RAMA__ y __REPO__ antes de enviarlo.
# Log en la instancia: /var/log/cloud-init-output.log
#
# Ojo: sin "set -x" a proposito, para que la clave del admin no quede escrita en el log.
set -euo pipefail

REPO="__REPO__"
RAMA="__RAMA__"
DIR=/opt/smartflet

echo ">> 1/5 Memoria swap de 2 GB (Java + Mongo + la compilacion no caben en 1 GB)"
if [ ! -f /swapfile ]; then
  dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

echo ">> 2/5 Docker, Docker Compose y git"
dnf install -y docker git
systemctl enable --now docker
mkdir -p /usr/local/lib/docker/cli-plugins
ARQ=$(uname -m)
curl -fsSL "https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-${ARQ}" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
BX_ARQ=$([ "$ARQ" = "aarch64" ] && echo arm64 || echo amd64)
curl -fsSL "https://github.com/docker/buildx/releases/download/v0.17.1/buildx-v0.17.1.linux-${BX_ARQ}" \
  -o /usr/local/lib/docker/cli-plugins/docker-buildx
chmod +x /usr/local/lib/docker/cli-plugins/*

echo ">> 3/5 Codigo desde GitHub (rama ${RAMA})"
rm -rf "$DIR"
git clone --depth 1 --branch "$RAMA" "$REPO" "$DIR"
cd "$DIR"

echo ">> 4/5 Archivo .env con la clave del admin leida de Parameter Store"
TOKEN=$(curl -sX PUT http://169.254.169.254/latest/api/token -H "X-aws-ec2-metadata-token-ttl-seconds: 300")
REGION=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/placement/region)
ADMIN_CLAVE=$(aws ssm get-parameter --region "$REGION" --name /smartflet/admin-clave \
  --with-decryption --query Parameter.Value --output text)
umask 077
cat > .env <<EOF
ADMIN_USUARIO=admin
ADMIN_NOMBRE=Administrador
ADMIN_CLAVE=${ADMIN_CLAVE}
SESION_MINUTOS=30
COOKIE_SEGURA=false
EOF

echo ">> 5/5 Construir y arrancar (tarda varios minutos la primera vez)"
docker compose up -d --build
echo ">> SmartFlet listo en el puerto 80"
