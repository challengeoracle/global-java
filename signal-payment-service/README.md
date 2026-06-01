# SIGNAL Payment Service

Servico financeiro do OffPay. Ele processa pagamentos, controla carteira e devolve o status financeiro para o restante da operacao.

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

| Funcao | Endpoint |
|--------|----------|
| Minha carteira principal | `GET /wallet/me` |
| Minha carteira pessoal | `GET /wallet/personal/me` |
| Depositar saldo | `POST /wallet/deposit` |
| Liquidar saldo pendente da loja | `POST /wallet/settle` |
| Minhas movimentacoes da carteira | `GET /wallet/transactions/me` |
| Minhas movimentacoes da carteira paginado | `GET /wallet/transactions/me/page` |
| Minhas movimentacoes pessoais | `GET /wallet/transactions/personal/me` |
| Minhas movimentacoes pessoais paginado | `GET /wallet/transactions/personal/me/page` |
| Minhas transacoes de pagamento | `GET /payment/transactions/me` |
| Minhas transacoes de pagamento paginado | `GET /payment/transactions/me/page` |
| Transacao por pedido | `GET /payment/transactions/order/{orderId}` |

---

## Papel no fluxo

O `payment-service` so fecha a parte financeira quando a operacao chega aos servicos centrais.

Ele faz isto:

- consome evento de pedido vindo do `sales-service`
- verifica saldo e aprova ou rejeita o pagamento
- debita a carteira do cliente quando houver saldo suficiente
- credita `pendingBalance` para a loja
- publica o resultado para o `sales-service`

No fluxo offline-first, isso significa que pagamento confirmado sempre depende da sincronizacao e da validacao online.

---

## Integracao com Sales

O servico participa destes passos:

1. recebe `PaymentRequested`
2. processa a transacao
3. grava pagamento e movimentacoes de carteira
4. publica `PaymentProcessed`

Se houver falha temporaria de mensageria, os eventos ficam em outbox para reenvio.

---

## Seguranca

Autenticados:

```text
/wallet/**
/payment/transactions/**
```

Publicos:

```text
/swagger-ui.html
/v3/api-docs
```

---

## Configuracao

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
