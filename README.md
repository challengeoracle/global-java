# OFFPAY Insights

Microsserviço de IA generativa da solução OffPay, desenvolvido para a Global Solution 26/1 na trilha de Disruptive Architectures: IoT, IoB & Generative IA. Este serviço faz parte de uma arquitetura baseada em microsserviços e depende da execução do projeto completo para funcionar corretamente.

## Integrantes do grupo

- `RM561061` – Arthur Thomas Mariano de Souza
- `RM559873` – Davi Cavalcanti Jorge
- `RM559728` – Mateus da Silveira Lima

## Vídeo demonstrativo

Acesso no YouTube: `INSERIR A URL AQUI`

## Objetivo do projeto

O OffPay Insights transforma dados operacionais em análises compreensíveis para clientes e vendedores. Usando IA Generativa com Spring AI, o assistente responde perguntas em linguagem natural sobre vendas, pagamentos, carteira digital e saldo pendente.

Esta entrega atende à proposta da trilha de IA Generativa, com foco em uma solução funcional conectada a um problema real e integrada a software, dados, APIs, banco de dados, automação e processamento contextual de informações.

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

## Papel deste repositório

Este repositório contém a solução completa do projeto OffPay em arquitetura de microsserviços. Embora o foco desta entrega de IA esteja no `signal-analytics-ai-service`, o professor precisará subir o projeto inteiro para validar as integrações, o fluxo entre serviços e as respostas geradas pela IA.

Serviços da solução:

| Serviço | Porta | Papel |
|--------|-------|-------|
| `signal-auth-service` | `8081` | autenticação, login, JWT e identidade |
| `signal-sales-service` | `8082` | catálogo, produtos, categorias e pedidos |
| `signal-payment-service` | `8083` | carteira, transações e processamento financeiro |
| `signal-analytics-ai-service` | `8084` | analytics, insights e respostas em linguagem natural |
| Oracle Free | `1521` | persistência de dados |
| RabbitMQ | `5672` | mensageria entre serviços |
| RabbitMQ Management | `15672` | painel de administração |

## Execução do projeto

### Pré-requisitos

- Docker Desktop instalado
- Docker Desktop em execução
- Git instalado

### Passo a passo

1. Clone o repositório:

```bash
git clone https://github.com/challengeoracle/global-devops
```

2. Entre na pasta do projeto:

```bash
cd global-devops
```

3. Copie o arquivo de variáveis de ambiente:

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

No Linux/macOS:

```bash
cp .env.example .env
```

4. Ajuste no `.env` pelo menos estas variáveis:

- `JWT_SECRET`
- `ORACLE_PASSWORD`
- `ORACLE_APP_PASSWORD`
- `GROQ_API_KEY`

5. Na raiz do projeto, suba o ambiente completo:

```powershell
docker compose up -d --build
```

6. Aguarde a subida de todos os containers:

- Oracle Free
- RabbitMQ
- `signal-auth-service`
- `signal-sales-service`
- `signal-payment-service`
- `signal-analytics-ai-service`

7. Verifique os logs, se necessário:

```powershell
docker compose logs -f
```

8. Acesse os serviços:

- Auth: `http://localhost:8081/swagger-ui.html`
- Sales: `http://localhost:8082/swagger-ui.html`
- Payment: `http://localhost:8083/swagger-ui.html`
- Analytics AI: `http://localhost:8084/swagger-ui.html`
- RabbitMQ Management: `http://localhost:15672`

### Encerramento do ambiente

```powershell
docker compose down
```

Para remover também o volume do banco:

```powershell
docker compose down -v
```

## Endpoints do OFFPAY Insights

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

## Estrutura DevOps preparada

- `docker-compose.yml`: orquestração local completa
- `dockerfiles/`: Dockerfiles de cada microsserviço
- `scripts/init-local-oracle.sh`: bootstrap do banco local
- `scripts/start-local.ps1`: atalho para Windows
- `scripts/start-local.sh`: atalho para Linux/macOS
- `.env.example`: modelo de configuração

## Entrega esperada no portal

Esta solução foi preparada para apoiar a entrega com:

- documentação do projeto
- arquitetura da solução
- instruções de execução
- espaço para link do vídeo demonstrativo
- uso de Inteligência Artificial Generativa aplicada a um problema real

## Próximos ajustes para entrega final

- Inserir o link público do vídeo no campo indicado
- Adicionar, se desejado, o link do repositório público final
- Complementar com diagramas adicionais, caso o grupo queira detalhar mais a arquitetura
