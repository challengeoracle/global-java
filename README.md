# SIGNAL

Conjunto de microsservicos do OffPay. O projeto foi feito para manter catalogo, pedidos, pagamentos e insights funcionando com foco em operacao offline-first.

Servicos locais:

```
signal-auth-service        http://localhost:8081
signal-sales-service       http://localhost:8082
signal-payment-service     http://localhost:8083
signal-analytics-ai-service http://localhost:8084
```

RabbitMQ local:

```
AMQP     http://localhost:5672
Painel   http://localhost:15672
```

---

## O que cada servico faz

| Servico | Papel |
|--------|-------|
| `signal-auth-service` | login, JWT e perfil autenticado |
| `signal-sales-service` | catalogo, pedidos online e sincronizacao offline |
| `signal-payment-service` | carteira, transacoes e processamento financeiro |
| `signal-analytics-ai-service` | resumos, analytics e perguntas para o OffPay Insights |

---

## Fluxo do projeto

O fluxo principal hoje e este:

1. o usuario autentica no `auth-service`
2. o vendedor trabalha com catalogo e pedidos no `sales-service`
3. quando um pedido chega aos servicos centrais, o `payment-service` processa o pagamento
4. o `analytics-ai-service` consolida dados e responde perguntas no app

No mobile, a venda pode nascer offline e ser sincronizada depois.

---

## Swagger

```
Auth       http://localhost:8081/swagger-ui.html
Sales      http://localhost:8082/swagger-ui.html
Payment    http://localhost:8083/swagger-ui.html
Analytics  http://localhost:8084/swagger-ui.html
```

---

## Infra local

Subir RabbitMQ:

```bash
docker compose up -d
```

Credenciais padrao do painel:

```text
guest
guest
```
