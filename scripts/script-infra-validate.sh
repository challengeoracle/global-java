#!/usr/bin/env bash

set -euo pipefail

# Mostra os recursos criados para facilitar a checagem antes do video.

SUFFIX="${SUFFIX:-rm559728}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-offpay-${SUFFIX}}"

echo "Resource Group:"
az group show --name "${RESOURCE_GROUP}" --output table

echo
echo "App Service Plan:"
az appservice plan list --resource-group "${RESOURCE_GROUP}" --output table

echo
echo "Web Apps:"
az webapp list --resource-group "${RESOURCE_GROUP}" --output table

echo
echo "Container Instances:"
az container list --resource-group "${RESOURCE_GROUP}" --output table

echo
echo "Container Registries:"
az acr list --resource-group "${RESOURCE_GROUP}" --output table
