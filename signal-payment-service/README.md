# SIGNAL Payment Service

Serviço financeiro do OffPay. Ele processa pagamentos, controla carteira e devolve o status financeiro para o restante da operação.

## Endereço local

```text
http://localhost:8083
```

## Swagger local

```text
http://localhost:8083/swagger-ui/index.html
```

## Responsabilidade

O `signal-payment-service` responde por:

| Função | Endpoint |
|--------|----------|
| Minha carteira principal | `GET /wallet/me` |
| Minha carteira pessoal | `GET /wallet/personal/me` |
| Depositar saldo | `POST /wallet/deposit` |
| Liquidar saldo pendente da loja | `POST /wallet/settle` |
| Minhas movimentações da carteira | `GET /wallet/transactions/me` |
| Minhas movimentações pessoais | `GET /wallet/transactions/personal/me` |
| Minhas transações de pagamento | `GET /payment/transactions/me` |
| Transação por pedido | `GET /payment/transactions/order/{orderId}` |

## Papel no fluxo

O `payment-service` fecha a parte financeira da operação quando o pedido chega aos serviços centrais.

Ele faz isto:

- consome evento de pedido vindo do `sales-service`
- verifica saldo e aprova ou rejeita o pagamento
- debita a carteira do cliente quando houver saldo suficiente
- credita `pendingBalance` para a loja
- publica o resultado para o `sales-service`

No fluxo offline-first, isso significa que pagamento confirmado sempre depende da sincronização e da validação online.

## Integração com Sales

O serviço participa destes passos:

1. recebe `PaymentRequested`
2. processa a transação
3. grava pagamento e movimentações de carteira
4. publica `PaymentProcessed`

Se houver falha temporária de mensageria, os eventos ficam em outbox para reenvio.

## Segurança

Autenticados:

```text
/wallet/**
/payment/transactions/**
```

Públicos:

```text
/swagger-ui/index.html
/v3/api-docs
```

## Configuração

```text
SERVER_PORT=8083
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
AUTH_SERVICE_URL=http://localhost:8081
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

## Execução local

Este serviço faz parte do `docker-compose` da raiz e pode ser iniciado com:

```powershell
docker compose up -d --build signal-payment-service
```

Variáveis esperadas no ambiente:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `AUTH_SERVICE_URL`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
