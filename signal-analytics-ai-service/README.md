# SIGNAL Analytics AI Service

Serviço de analytics e insights do OffPay. Ele junta dados de pedidos, pagamentos e carteira para mostrar resumos e responder perguntas no app.

A API roda localmente em:

```
http://localhost:8084
```

Swagger:

```
http://localhost:8084/swagger-ui.html
```

---

## Responsabilidade

O `signal-analytics-ai-service` responde por:

| Função | Endpoint |
|--------|----------|
| Resumo geral do usuário | `GET /analytics/me/summary` |
| Resumo do vendedor | `GET /analytics/seller/summary` |
| Resumo do cliente | `GET /analytics/customer/summary` |
| Produtos mais vendidos da loja | `GET /analytics/seller/top-products` |
| Gastos do cliente | `GET /analytics/customer/spending` |
| Perguntar ao OffPay Insights | `POST /ai/insights/ask` |

---

## Papel no fluxo

Esse serviço não cria pedido nem processa pagamento. Ele fica por cima da operação para:

- consolidar indicadores do vendedor e do cliente
- mostrar saldo, pedidos pagos, rejeitados e pendentes
- resumir consumo e produto mais recorrente
- responder perguntas com contexto do OffPay

O endpoint de IA usa:

- dados reais de analytics
- regras do runtime do projeto
- tools do Spring AI

---

## Integração com os outros serviços

O `analytics-ai-service` consome:

- `auth-service` para identificar o usuário autenticado
- `sales-service` para pedidos, vendas e catálogo
- `payment-service` para carteira e transações

Ele não exige outro deploy separado além dele mesmo.

---

## MCP e Insights

O serviço também expõe recursos de MCP no mesmo processo. Isso serve para o módulo de insights consultar regras operacionais e documentos internos do OffPay sem depender de outro serviço.

Os endpoints principais do app continuam sendo os de `analytics` e `ai/insights`.

---

## Segurança

Autenticados:

```text
/analytics/**
/ai/insights/ask
```

Públicos:

```text
/swagger-ui.html
/v3/api-docs
```

---

## Configuração

```yaml
server.port=8084
JWT_SECRET=
AUTH_SERVICE_URL=http://localhost:8081
SALES_SERVICE_URL=http://localhost:8082
PAYMENT_SERVICE_URL=http://localhost:8083
GROQ_API_KEY=
GROQ_BASE_URL=https://api.groq.com/openai/v1
GROQ_MODEL=llama-3.1-8b-instant
```

## Docker

Este serviço faz parte do `docker-compose` da raiz e sobe com:

```powershell
docker compose up -d --build signal-analytics-ai-service
```

Variáveis esperadas no ambiente:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `AUTH_SERVICE_URL`
- `SALES_SERVICE_URL`
- `PAYMENT_SERVICE_URL`
- `GROQ_API_KEY`
- `GROQ_BASE_URL`
- `GROQ_MODEL`
