#!/usr/bin/env bash

set -euo pipefail

# Infraestrutura base do OffPay no Azure
# Recursos criados:
# - Resource Group
# - Azure Container Registry
# - App Service Plan Linux

LOCATION="${LOCATION:-southafricanorth}"
SUFFIX="${SUFFIX:-rm559728}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-offpay-${SUFFIX}}"
ACR_NAME="${ACR_NAME:-offpayacr${SUFFIX}}"
APP_SERVICE_PLAN="${APP_SERVICE_PLAN:-plan-offpay-${SUFFIX}}"
APP_SERVICE_SKU="${APP_SERVICE_SKU:-B1}"

echo "Criando Resource Group ${RESOURCE_GROUP} em ${LOCATION}..."
az group create \
  --name "${RESOURCE_GROUP}" \
  --location "${LOCATION}"

echo "Criando Azure Container Registry ${ACR_NAME}..."
az acr create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${ACR_NAME}" \
  --sku Basic \
  --admin-enabled true

echo "Criando App Service Plan ${APP_SERVICE_PLAN}..."
az appservice plan create \
  --name "${APP_SERVICE_PLAN}" \
  --resource-group "${RESOURCE_GROUP}" \
  --location "${LOCATION}" \
  --sku "${APP_SERVICE_SKU}" \
  --is-linux

echo
echo "Infra base criada com sucesso."
echo "RESOURCE_GROUP=${RESOURCE_GROUP}"
echo "ACR_NAME=${ACR_NAME}"
echo "APP_SERVICE_PLAN=${APP_SERVICE_PLAN}"
