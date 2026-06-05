# OFFPAY Insights

Microsserviço de IA generativa da solução OffPay, desenvolvido para consolidar dados operacionais e responder perguntas em linguagem natural sobre vendas, pagamentos, carteira digital e saldo pendente.

## Objetivo do serviço

O OffPay Insights transforma dados operacionais em análises compreensíveis para clientes e vendedores. Usando Spring AI, o assistente responde perguntas em linguagem natural sobre vendas, pagamentos, carteira digital e saldo pendente.

## Como funciona

A IA consulta os microsserviços existentes via Feign, recupera regras operacionais de uma base local de conhecimento em PDF e monta um contexto estruturado para gerar uma resposta em linguagem natural.

A solução não utiliza RAG vetorial. Em vez disso, usa recuperação textual por trechos de PDF armazenados em banco, com busca por termos. O serviço também expõe MCP no mesmo processo, permitindo integração com ferramentas externas compatíveis com o protocolo.

Fluxo principal:

`Mobile -> Analytics AI Service -> Auth / Sales / Payment -> Contexto + PDF -> Spring AI -> Resposta`

## Dados consultados

- `Auth Service`: perfil do usuário autenticado e loja vinculada
- `Sales Service`: pedidos, compras, vendas e itens vendidos
- `Payment Service`: carteira, saldo, saldo pendente e transações de pagamento
- base local de conhecimento: regras operacionais do OffPay extraídas de PDF

## Endereço local

```text
http://localhost:8084
```

## Swagger local

```text
http://localhost:8084/swagger-ui/index.html
```

## Endpoints principais

- `GET /analytics/me/summary`
- `GET /analytics/me/summary/resource`
- `GET /analytics/me/summary/period`
- `GET /analytics/me/chart`
- `GET /analytics/seller/summary`
- `GET /analytics/customer/summary`
- `GET /analytics/seller/top-products`
- `GET /analytics/seller/chart`
- `GET /analytics/customer/spending`
- `GET /analytics/customer/chart`
- `POST /ai/insights/ask`

## Configuração

```text
SERVER_PORT=8084
JWT_SECRET=
AUTH_SERVICE_URL=http://localhost:8081
SALES_SERVICE_URL=http://localhost:8082
PAYMENT_SERVICE_URL=http://localhost:8083
GROQ_API_KEY=
GROQ_BASE_URL=https://api.groq.com/openai
GROQ_MODEL=llama-3.1-8b-instant
```

## Execução local

Embora este documento foque no OffPay Insights, o microsserviço depende do ecossistema completo do OffPay. Para validação correta, o ideal é subir o projeto inteiro pela raiz do repositório:

```powershell
docker compose up -d --build
```

Variáveis esperadas no ambiente:

- `JWT_SECRET`
- `AUTH_SERVICE_URL`
- `SALES_SERVICE_URL`
- `PAYMENT_SERVICE_URL`
- `GROQ_API_KEY`
- `GROQ_BASE_URL`
- `GROQ_MODEL`
