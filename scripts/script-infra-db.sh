#!/usr/bin/env bash

set -euo pipefail

# Cria Azure SQL Server + Azure SQL Database no mesmo estilo do projeto de referencia.

LOCATION="${LOCATION:-southafricanorth}"
SUFFIX="${SUFFIX:-rm559728}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-offpay-${SUFFIX}}"
SQL_SERVER_NAME="${SQL_SERVER_NAME:-sql-offpay-${SUFFIX}}"
SQL_DATABASE_NAME="${SQL_DATABASE_NAME:-sqldb-offpay-${SUFFIX}}"
SQL_ADMIN_USER="${SQL_ADMIN_USER:-sqladminoffpay}"
SQL_ADMIN_PASSWORD="${SQL_ADMIN_PASSWORD:-}"
SQL_SKU="${SQL_SKU:-Basic}"
FIREWALL_RULE_NAME="${FIREWALL_RULE_NAME:-allowAzureAndPublic}"

if [[ -z "${SQL_ADMIN_PASSWORD}" ]]; then
  echo "Defina SQL_ADMIN_PASSWORD antes de executar."
  exit 1
fi

echo "Criando SQL Server ${SQL_SERVER_NAME}..."
az sql server create \
  --name "${SQL_SERVER_NAME}" \
  --resource-group "${RESOURCE_GROUP}" \
  --location "${LOCATION}" \
  --admin-user "${SQL_ADMIN_USER}" \
  --admin-password "${SQL_ADMIN_PASSWORD}" \
  --enable-public-network true

echo "Criando SQL Database ${SQL_DATABASE_NAME}..."
az sql db create \
  --resource-group "${RESOURCE_GROUP}" \
  --server "${SQL_SERVER_NAME}" \
  --name "${SQL_DATABASE_NAME}" \
  --service-objective "${SQL_SKU}" \
  --backup-storage-redundancy Local \
  --zone-redundant false

echo "Criando firewall rule ${FIREWALL_RULE_NAME}..."
az sql server firewall-rule create \
  --resource-group "${RESOURCE_GROUP}" \
  --server "${SQL_SERVER_NAME}" \
  --name "${FIREWALL_RULE_NAME}" \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 255.255.255.255

echo
echo "Banco Azure SQL criado com sucesso."
echo "SQL_SERVER_NAME=${SQL_SERVER_NAME}"
echo "SQL_DATABASE_NAME=${SQL_DATABASE_NAME}"
echo "DB_URL=jdbc:sqlserver://${SQL_SERVER_NAME}.database.windows.net:1433;database=${SQL_DATABASE_NAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
echo "DB_USERNAME=${SQL_ADMIN_USER}"
