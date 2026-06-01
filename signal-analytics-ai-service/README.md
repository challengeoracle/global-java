# SIGNAL Analytics AI Service

Servico de analytics e insights do OffPay. Ele junta dados de pedidos, pagamentos e carteira para mostrar resumos e responder perguntas no app.

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

| Funcao | Endpoint |
|--------|----------|
| Resumo geral do usuario | `GET /analytics/me/summary` |
| Resumo do vendedor | `GET /analytics/seller/summary` |
| Resumo do cliente | `GET /analytics/customer/summary` |
| Produtos mais vendidos da loja | `GET /analytics/seller/top-products` |
| Gastos do cliente | `GET /analytics/customer/spending` |
| Perguntar ao OffPay Insights | `POST /ai/insights/ask` |

---

## Papel no fluxo

Esse servico nao cria pedido nem processa pagamento. Ele fica por cima da operacao para:

- consolidar indicadores do vendedor e do cliente
- mostrar saldo, pedidos pagos, rejeitados e pendentes
- resumir consumo e produto mais recorrente
- responder perguntas com contexto do OffPay

O endpoint de IA usa:

- dados reais de analytics
- regras do runtime do projeto
- tools do Spring AI

---

## Integracao com os outros servicos

O `analytics-ai-service` consome:

- `auth-service` para identificar o usuario autenticado
- `sales-service` para pedidos, vendas e catalogo
- `payment-service` para carteira e transacoes

Ele nao exige outro deploy separado alem dele mesmo.

---

## MCP e Insights

O servico tambem expõe recursos de MCP no mesmo processo. Isso serve para o modulo de insights consultar regras operacionais e documentos internos do OffPay sem depender de outro servico.

Os endpoints principais do app continuam sendo os de `analytics` e `ai/insights`.

---

## Seguranca

Autenticados:

```text
/analytics/**
/ai/insights/ask
```

Publicos:

```text
/swagger-ui.html
/v3/api-docs
```

---

## Configuracao

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
