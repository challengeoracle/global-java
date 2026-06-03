#!/usr/bin/env bash

set -euo pipefail

# Cria RabbitMQ em Azure Container Instances
# Exposicao publica para simplificar a integracao entre os Web Apps.

LOCATION="${LOCATION:-southafricanorth}"
SUFFIX="${SUFFIX:-rm559728}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-offpay-${SUFFIX}}"
RABBIT_CONTAINER_GROUP="${RABBIT_CONTAINER_GROUP:-aci-rabbitmq-offpay-${SUFFIX}}"
RABBIT_DNS_LABEL="${RABBIT_DNS_LABEL:-rabbitmq-offpay-${SUFFIX}}"
RABBITMQ_USERNAME="${RABBITMQ_USERNAME:-guest}"
RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD:-guest12345!}"

echo "Criando RabbitMQ em Container Instance..."
az container create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${RABBIT_CONTAINER_GROUP}" \
  --image rabbitmq:3-management \
  --location "${LOCATION}" \
  --dns-name-label "${RABBIT_DNS_LABEL}" \
  --ports 5672 15672 \
  --ip-address Public \
  --cpu 1 \
  --memory 2 \
  --environment-variables \
    RABBITMQ_DEFAULT_USER="${RABBITMQ_USERNAME}" \
    RABBITMQ_DEFAULT_PASS="${RABBITMQ_PASSWORD}"

echo
echo "RabbitMQ criado com sucesso."
echo "AMQP: ${RABBIT_DNS_LABEL}.${LOCATION}.azurecontainer.io:5672"
echo "Management: http://${RABBIT_DNS_LABEL}.${LOCATION}.azurecontainer.io:15672"
