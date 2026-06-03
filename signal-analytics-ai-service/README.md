# OFFPAY Insights

Microsserviço de IA generativa da solução OffPay, desenvolvido para a Global Solution 26/1 na trilha de Disruptive Architectures: IoT, IoB & Generative IA.

## Integrantes do grupo

- `RM561061` – Arthur Thomas Mariano de Souza
- `RM559873` – Davi Cavalcanti Jorge
- `RM559728` – Mateus da Silveira Lima

## Vídeo demonstrativo

Acesso no YouTube: `INSERIR A URL AQUI`

## Objetivo do projeto

O OffPay Insights transforma dados operacionais em análises compreensíveis para clientes e vendedores. Usando IA Generativa com Spring AI, o assistente responde perguntas em linguagem natural sobre vendas, pagamentos, carteira digital e saldo pendente.

O projeto atende à proposta da trilha de IA Generativa ao integrar modelos generativos com APIs, banco de dados, processamento contextual e interface de consulta baseada em linguagem natural.

## Problema

Após operar offline, comerciantes precisam entender o que aconteceu: quanto venderam, quais produtos saíram, quais pagamentos foram aprovados, o que está pendente e o que foi sincronizado. O OffPay Insights entrega essas respostas de forma simples.

## Como funciona

A IA consulta os microsserviços existentes via Feign, recupera regras operacionais de uma base local de conhecimento em PDF e monta um contexto estruturado para gerar uma resposta em linguagem natural.

A solução não utiliza RAG vetorial. Em vez disso, usa recuperação textual por trechos de PDF armazenados em banco, com busca por termos. O serviço também expõe MCP no mesmo processo, permitindo integração com ferramentas externas compatíveis com o protocolo.

Fluxo principal:

`Mobile -> Analytics AI Service -> Auth / Sales / Payment -> Contexto + PDF -> Spring AI -> Resposta`

## Arquitetura da solução

![Arquitetura OffPay Insights](https://media.discordapp.net/attachments/1417871654817894594/1511562961091694733/image.png?ex=6a20e835&is=6a1f96b5&hm=28a66641593ab5e0f691a1e59a5fc45654553258add1178c2d704e25daa57599&=&format=webp&quality=lossless&width=1573&height=353.png)

## Dados consultados

- `Auth Service`: perfil do usuário autenticado e loja vinculada
- `Sales Service`: pedidos, compras, vendas e itens vendidos
- `Payment Service`: carteira, saldo, saldo pendente e transações de pagamento
- Base local de conhecimento: regras operacionais do OffPay extraídas de PDF

## Exemplos de uso

Vendedor:

- Pergunta: `Quanto vendi hoje?`
- Resposta: `Hoje sua loja vendeu R$ 67,42 em 22 pedidos.`

Cliente:

- Pergunta: `Quanto gastei esse mês?`
- Resposta: `Você gastou R$ 420,69 em 13 pedidos.`

## Endpoints

- `GET /analytics/me/summary` – resumo do usuário autenticado
- `GET /analytics/me/summary/resource` – resumo com links HATEOAS
- `GET /analytics/me/summary/period` – resumo por período
- `GET /analytics/me/chart` – gráfico do usuário autenticado
- `GET /analytics/seller/summary` – indicadores da loja
- `GET /analytics/customer/summary` – consumo do cliente
- `GET /analytics/seller/top-products` – produtos mais vendidos
- `GET /analytics/seller/chart` – gráfico do vendedor
- `GET /analytics/customer/spending` – histórico de gastos
- `GET /analytics/customer/chart` – gráfico do cliente
- `POST /ai/insights/ask` – resposta em linguagem natural

## Execução do microsserviço

Embora este documento foque no OffPay Insights, o microsserviço depende do ecossistema completo do OffPay. Para validação correta, é necessário subir o projeto inteiro pela raiz do repositório.

### URLs principais

- API local: `http://localhost:8084`
- Swagger: `http://localhost:8084/swagger-ui.html`

### Variáveis de ambiente relevantes

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

### Subida com Docker

Na raiz do projeto:

```powershell
docker compose up -d --build
```

## Relação com a entrega da disciplina

Esta solução demonstra:

- uso de Inteligência Artificial Generativa aplicada a um problema real
- integração entre software, dados, banco de dados e APIs
- processamento contextual com base de conhecimento em PDF
- arquitetura orientada a microsserviços
- documentação e fluxo de execução para demonstração em vídeo
