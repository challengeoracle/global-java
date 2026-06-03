# SIGNAL Payment Service

Serviço financeiro do OffPay. Ele processa pagamentos, controla carteira e devolve o status financeiro para o restante da operação.

A API roda localmente em:

```
http://localhost:8083
```

Swagger:

```
http://localhost:8083/swagger-ui.html
```

---

## Responsabilidade

O `signal-payment-service` responde por:

| Função | Endpoint |
|--------|----------|
| Minha carteira principal | `GET /wallet/me` |
| Minha carteira pessoal | `GET /wallet/personal/me` |
| Depositar saldo | `POST /wallet/deposit` |
| Liquidar saldo pendente da loja | `POST /wallet/settle` |
| Minhas movimentações da carteira | `GET /wallet/transactions/me` |
| Minhas movimentações da carteira paginado | `GET /wallet/transactions/me/page` |
| Minhas movimentações pessoais | `GET /wallet/transactions/personal/me` |
| Minhas movimentações pessoais paginado | `GET /wallet/transactions/personal/me/page` |
| Minhas transações de pagamento | `GET /payment/transactions/me` |
| Minhas transações de pagamento paginado | `GET /payment/transactions/me/page` |
| Transação por pedido | `GET /payment/transactions/order/{orderId}` |

---

## Papel no fluxo

O `payment-service` só fecha a parte financeira quando a operação chega aos serviços centrais.

Ele faz isto:

- consome evento de pedido vindo do `sales-service`
- verifica saldo e aprova ou rejeita o pagamento
- debita a carteira do cliente quando houver saldo suficiente
- credita `pendingBalance` para a loja
- publica o resultado para o `sales-service`

No fluxo offline-first, isso significa que pagamento confirmado sempre depende da sincronização e da validação online.

---

## Integração com Sales

O serviço participa destes passos:

1. recebe `PaymentRequested`
2. processa a transação
3. grava pagamento e movimentações de carteira
4. publica `PaymentProcessed`

Se houver falha temporária de mensageria, os eventos ficam em outbox para reenvio.

---

## Segurança

Autenticados:

```text
/wallet/**
/payment/transactions/**
```

Públicos:

```text
/swagger-ui.html
/v3/api-docs
```

---

## Configuração

```yaml
server.port=8083
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

## Docker

Este serviço faz parte do `docker-compose` da raiz e sobe com:

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
