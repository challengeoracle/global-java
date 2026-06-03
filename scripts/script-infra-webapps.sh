#!/usr/bin/env bash

set -euo pipefail

# Cria os quatro Web Apps Linux para os microsservicos do OffPay.
# Usa imagem temporaria do nginx para bootstrap; a pipeline depois substitui
# pela imagem real publicada no ACR.

LOCATION="${LOCATION:-southafricanorth}"
SUFFIX="${SUFFIX:-rm559728}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-offpay-${SUFFIX}}"
APP_SERVICE_PLAN="${APP_SERVICE_PLAN:-plan-offpay-${SUFFIX}}"
ACR_NAME="${ACR_NAME:-offpayacr${SUFFIX}}"

AUTH_APP_NAME="${AUTH_APP_NAME:-app-offpay-auth-${SUFFIX}}"
SALES_APP_NAME="${SALES_APP_NAME:-app-offpay-sales-${SUFFIX}}"
PAYMENT_APP_NAME="${PAYMENT_APP_NAME:-app-offpay-payment-${SUFFIX}}"
ANALYTICS_APP_NAME="${ANALYTICS_APP_NAME:-app-offpay-analytics-${SUFFIX}}"

RABBIT_DNS_LABEL="${RABBIT_DNS_LABEL:-rabbitmq-offpay-${SUFFIX}}"
RABBITMQ_HOST="${RABBITMQ_HOST:-${RABBIT_DNS_LABEL}.${LOCATION}.azurecontainer.io}"
RABBITMQ_PORT="${RABBITMQ_PORT:-5672}"
RABBITMQ_USERNAME="${RABBITMQ_USERNAME:-guest}"
RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD:-guest12345!}"

DB_URL="${DB_URL:-}"
DB_USERNAME="${DB_USERNAME:-}"
DB_PASSWORD="${DB_PASSWORD:-}"
JWT_SECRET="${JWT_SECRET:-}"
GROQ_API_KEY="${GROQ_API_KEY:-}"
DB_DRIVER_CLASS_NAME="${DB_DRIVER_CLASS_NAME:-com.microsoft.sqlserver.jdbc.SQLServerDriver}"
DB_DIALECT="${DB_DIALECT:-org.hibernate.dialect.SQLServerDialect}"
FLYWAY_LOCATIONS_AUTH="${FLYWAY_LOCATIONS_AUTH:-classpath:db/migration-sqlserver}"
FLYWAY_LOCATIONS_SALES="${FLYWAY_LOCATIONS_SALES:-classpath:db/migration-sqlserver}"
FLYWAY_LOCATIONS_PAYMENT="${FLYWAY_LOCATIONS_PAYMENT:-classpath:db/migration-sqlserver}"
FLYWAY_LOCATIONS_ANALYTICS="${FLYWAY_LOCATIONS_ANALYTICS:-classpath:db/migration-sqlserver}"

if [[ -z "${DB_URL}" || -z "${DB_USERNAME}" || -z "${DB_PASSWORD}" || -z "${JWT_SECRET}" ]]; then
  echo "Defina DB_URL, DB_USERNAME, DB_PASSWORD e JWT_SECRET antes de executar."
  exit 1
fi

create_webapp() {
  local app_name="$1"
  local flyway_locations="$2"
  local server_port="$3"

  echo "Criando Web App ${app_name}..."
  az webapp create \
    --name "${app_name}" \
    --resource-group "${RESOURCE_GROUP}" \
    --plan "${APP_SERVICE_PLAN}" \
    --deployment-container-image-name "nginx:latest"

  echo "Configurando container no Web App ${app_name}..."
  az webapp config container set \
    --name "${app_name}" \
    --resource-group "${RESOURCE_GROUP}" \
    --container-image-name "nginx:latest"

  echo "Configurando app settings no Web App ${app_name}..."
  az webapp config appsettings set \
    --name "${app_name}" \
    --resource-group "${RESOURCE_GROUP}" \
    --settings \
      WEBSITES_PORT="${server_port}" \
      SERVER_PORT="${server_port}" \
      WEBSITES_CONTAINER_START_TIME_LIMIT=1800 \
      DB_URL="${DB_URL}" \
      DB_USERNAME="${DB_USERNAME}" \
      DB_PASSWORD="${DB_PASSWORD}" \
      DB_DRIVER_CLASS_NAME="${DB_DRIVER_CLASS_NAME}" \
      DB_DIALECT="${DB_DIALECT}" \
      FLYWAY_LOCATIONS="${flyway_locations}" \
      JWT_SECRET="${JWT_SECRET}" \
      JWT_EXPIRATION_MINUTES=120 \
      FLYWAY_ENABLED=true \
      RABBITMQ_HOST="${RABBITMQ_HOST}" \
      RABBITMQ_PORT="${RABBITMQ_PORT}" \
      RABBITMQ_USERNAME="${RABBITMQ_USERNAME}" \
      RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD}" \
      AUTH_SERVICE_URL="https://${AUTH_APP_NAME}.azurewebsites.net" \
      SALES_SERVICE_URL="https://${SALES_APP_NAME}.azurewebsites.net" \
      PAYMENT_SERVICE_URL="https://${PAYMENT_APP_NAME}.azurewebsites.net" \
      GROQ_API_KEY="${GROQ_API_KEY}" \
      GROQ_BASE_URL="https://api.groq.com/openai" \
      GROQ_MODEL="llama-3.1-8b-instant" \
      AI_TEMPERATURE=0.2 \
      AI_MAX_TOKENS=160 \
      AI_RETRIEVAL_MAX_CHUNKS=3
}

create_webapp "${AUTH_APP_NAME}" "${FLYWAY_LOCATIONS_AUTH}" "8081"
create_webapp "${SALES_APP_NAME}" "${FLYWAY_LOCATIONS_SALES}" "8082"
create_webapp "${PAYMENT_APP_NAME}" "${FLYWAY_LOCATIONS_PAYMENT}" "8083"
create_webapp "${ANALYTICS_APP_NAME}" "${FLYWAY_LOCATIONS_ANALYTICS}" "8084"

echo
echo "Web Apps criados com sucesso."
echo "AUTH_APP_NAME=${AUTH_APP_NAME}"
echo "SALES_APP_NAME=${SALES_APP_NAME}"
echo "PAYMENT_APP_NAME=${PAYMENT_APP_NAME}"
echo "ANALYTICS_APP_NAME=${ANALYTICS_APP_NAME}"
echo "ACR_NAME=${ACR_NAME}"
