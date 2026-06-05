# OFFPAY

Projeto acadêmico orientado a DevOps e cloud para manter a operação de pequenos comércios funcionando mesmo em cenários de instabilidade de rede, usando microsserviços, mensageria, autenticação centralizada e abordagem offline-first.

## Integrantes

- `RM561061` - Arthur Thomas Mariano de Souza
- `RM559873` - Davi Cavalcanti Jorge
- `RM559728` - Mateus da Silveira Lima

## Visão Geral

O OffPay foi pensado para reduzir perdas operacionais quando a internet falha durante o fluxo de venda. Em vez de depender de uma conexão estável o tempo todo, a solução separa responsabilidades em microsserviços e mantém a operação da loja com foco em continuidade, sincronização posterior e rastreabilidade.

Os domínios principais da solução são:

- autenticação e identidade
- catálogo, categorias, produtos e pedidos
- carteira e pagamentos
- analytics e insights com IA

## Problema Resolvido

Pequenos comércios perdem vendas quando a internet falha no momento do pedido, do pagamento ou da consulta ao catálogo. Isso gera filas, retrabalho, perda de confiança e risco operacional.

O OffPay trata esse problema com uma abordagem offline-first:

- mantém a operação comercial ativa
- registra informações para sincronização posterior
- reduz o impacto de instabilidades de rede
- separa fluxos críticos em serviços independentes

## Objetivos da Solução

- permitir que vendedores continuem vendendo mesmo com instabilidade
- sincronizar pedidos, catálogo e eventos quando a conectividade retornar
- registrar pagamentos com rastreabilidade
- manter uma separação clara entre autenticação, operação comercial, pagamentos e analytics
- viabilizar entrega automatizada em nuvem com Azure DevOps

## Microsserviços

| Serviço | Porta | Papel |
|---|---:|---|
| `signal-auth-service` | `8081` | autenticação, cadastro, login, JWT e identidade |
| `signal-sales-service` | `8082` | catálogo, categorias, produtos, pedidos e sincronização |
| `signal-payment-service` | `8083` | carteira, transações e processamento financeiro |
| `signal-analytics-ai-service` | `8084` | resumos, gráficos, analytics e insights |

## Arquitetura Macro

O desenho abaixo representa a arquitetura macro da solução publicada na Azure, incluindo Azure DevOps, Container Registry, App Services, Azure SQL e RabbitMQ.

![Arquitetura Macro da Solução OffPay na Azure](docs/offpay-azure-architecture.drawio.png)

## Justificativa da Arquitetura

A arquitetura de microsserviços foi adotada porque o problema envolve domínios diferentes e responsabilidades que precisam evoluir de forma relativamente independente.

- o `auth-service` centraliza identidade, JWT e segurança
- o `sales-service` concentra o fluxo operacional da loja
- o `payment-service` isola a regra financeira
- o `analytics-ai-service` consolida dados e responde perguntas analíticas

Essa divisão reduz acoplamento, melhora a manutenção e torna a solução mais adequada para uma entrega DevOps com build, artefatos, imagens Docker e deploy automatizado.

## Recursos Técnicos Aplicados

- Spring Boot
- APIs REST
- Spring Security com JWT
- Swagger / OpenAPI
- HATEOAS em endpoints selecionados
- RabbitMQ para mensageria
- Feign Client para comunicação entre serviços
- Flyway para versionamento do banco
- Docker e Docker Compose
- Azure DevOps Pipelines
- Azure Web Apps for Containers
- Azure SQL Database
- Azure Container Registry
- Spring AI no serviço de analytics

## Fluxo Resumido da Solução

1. o usuário se autentica no `signal-auth-service`
2. o vendedor opera categorias, produtos e pedidos no `signal-sales-service`
3. o `signal-payment-service` processa carteira e transações
4. o `signal-analytics-ai-service` consolida dados e gera resumos e insights
5. a entrega em nuvem acontece com build, publicação de artefatos, build/push das imagens e deploy automático na Azure

## Execução Local

O repositório deve ser executado como solução completa.

### Pré-requisitos

- Docker Desktop
- Git
- arquivo `.env` configurado

### Passo a passo

1. Clone o repositório:

```bash
git clone https://github.com/challengeoracle/global-java
cd global-java
```

2. Copie o arquivo de ambiente:

```powershell
Copy-Item .env.example .env
```

3. Ajuste pelo menos:

- `JWT_SECRET`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `GROQ_API_KEY`

4. Suba todos os serviços:

```powershell
docker compose up -d --build
```

5. Acompanhe os logs:

```powershell
docker compose logs -f
```

### Endpoints Locais

- Auth: `http://localhost:8081/swagger-ui.html`
- Sales: `http://localhost:8082/swagger-ui.html`
- Payment: `http://localhost:8083/swagger-ui.html`
- Analytics AI: `http://localhost:8084/swagger-ui.html`
- RabbitMQ Management: `http://localhost:15672`

## Banco de Dados

O projeto foi preparado para dois cenários:

- Oracle legado
- Azure SQL para a entrega em nuvem

As variáveis principais são:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_DRIVER_CLASS_NAME`
- `DB_DIALECT`
- `FLYWAY_LOCATIONS`

### Oracle

- `DB_DRIVER_CLASS_NAME=oracle.jdbc.OracleDriver`
- `DB_DIALECT=org.hibernate.dialect.OracleDialect`
- `FLYWAY_LOCATIONS=classpath:db/migration`

### Azure SQL

- `DB_DRIVER_CLASS_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver`
- `DB_DIALECT=org.hibernate.dialect.SQLServerDialect`
- `FLYWAY_LOCATIONS=classpath:db/migration-sqlserver`

## Provisionamento na Azure

Os recursos da entrega são criados por script via Azure CLI.

### Scripts obrigatórios

- `scripts/script-bd.sql`
- `scripts/script-infra-base.sh`
- `scripts/script-infra-db.sh`
- `scripts/script-infra-rabbitmq.sh`
- `scripts/script-infra-webapps.sh`
- `scripts/script-infra-validate.sh`

### Ordem recomendada

1. Infra base:

```bash
export LOCATION=southafricanorth
export SUFFIX=rm559728
bash scripts/script-infra-base.sh
```

2. Azure SQL:

```bash
export SQL_ADMIN_PASSWORD='SuaSenhaForteAqui123!'
bash scripts/script-infra-db.sh
```

3. RabbitMQ:

```bash
bash scripts/script-infra-rabbitmq.sh
```

4. Web Apps:

```bash
export DB_URL='jdbc:sqlserver://sql-offpay-rm559728.database.windows.net:1433;database=sqldb-offpay-rm559728;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;'
export DB_USERNAME='sqladminoffpay'
export DB_PASSWORD='SuaSenhaForteAqui123!'
export JWT_SECRET='sua-chave-base64'
export GROQ_API_KEY='sua-chave'
bash scripts/script-infra-webapps.sh
```

5. Validação:

```bash
bash scripts/script-infra-validate.sh
```

## Azure DevOps

### Organização esperada

- projeto privado no Azure DevOps
- código no Azure Repos
- task criada no Azure Boards
- branch de trabalho
- PR para `main`
- pipeline automática após merge

### Políticas da `main`

A `main` deve ficar protegida com:

- revisor obrigatório
- vínculo com work item
- merge por PR

### Pipeline

O arquivo `azure-pipeline.yml` implementa:

- trigger na `main`
- execução de testes
- publicação de resultados JUnit
- empacotamento dos serviços
- build e push das imagens Docker
- publicação de artefatos
- release automática nos Web Apps

### Service Connections

Crie:

- `Conexao-Azure-DevOps`
- `Conexao-ACR`

## Artefatos e Estrutura DevOps

- `docker-compose.yml`: orquestração local
- `dockerfiles/`: Dockerfiles por serviço
- `azure-pipeline.yml`: pipeline CI/CD
- `scripts/script-bd.sql`: DDL consolidado
- `scripts/script-infra-base.sh`: Resource Group, ACR e App Service Plan
- `scripts/script-infra-db.sh`: Azure SQL Server e Database
- `scripts/script-infra-rabbitmq.sh`: RabbitMQ em ACI
- `scripts/script-infra-webapps.sh`: criação e configuração dos Web Apps
- `scripts/script-infra-validate.sh`: validação dos recursos
- `.env.example`: modelo de variáveis

## CRUD em JSON para Demonstração

Para a gravação, a recomendação mais segura é demonstrar CRUD em:

- `TB_PRODUCT_CATEGORIES`
- `TB_PRODUCTS`

### Categoria - Create

`POST /category`

```json
{
  "name": "Bebidas",
  "description": "Categoria de bebidas da loja"
}
```

### Categoria - Read

`GET /category/me`

### Categoria - Update

`PUT /category/{id}`

```json
{
  "name": "Bebidas Geladas",
  "description": "Categoria atualizada para produtos refrigerados"
}
```

### Categoria - Delete

`DELETE /category/{id}`

### Produto - Create

`POST /product`

```json
{
  "categoryId": "11111111-1111-1111-1111-111111111111",
  "name": "Água 500ml",
  "description": "Garrafa de água sem gás",
  "price": 4.50,
  "stockQuantity": 100
}
```

### Produto - Read

`GET /product/{id}`

### Produto - Update

`PUT /product/{id}`

```json
{
  "categoryId": "11111111-1111-1111-1111-111111111111",
  "name": "Água 500ml Premium",
  "description": "Produto atualizado em nuvem",
  "price": 5.00,
  "stockQuantity": 120
}
```

### Produto - Delete

`DELETE /product/{id}`

## Validação Direta no Banco

Na gravação, a persistência deve ser comprovada também com `SELECT` no banco da Azure.

### Categoria

```sql
SELECT ID, NAME, DESCRIPTION, ACTIVE
FROM TB_PRODUCT_CATEGORIES;
```

### Produto

```sql
SELECT ID, NAME, PRICE, STOCK_QUANTITY, ACTIVE
FROM TB_PRODUCTS;
```

## Roteiro do Vídeo

O vídeo da entrega deve seguir esta ordem:

1. apresentar o `README`, a solução proposta e o desenho da arquitetura
2. mostrar no Portal Azure os recursos criados pelos scripts
3. criar uma nova task no Azure Boards
4. criar uma nova branch
5. simular uma alteração real em código fonte
6. fazer merge na `main`
7. executar automaticamente as Pipelines de Build e Release
8. mostrar a execução completa das pipelines
9. destacar artefatos publicados e testes executados
10. demonstrar a alteração publicada em nuvem
11. executar CRUD em pelo menos duas tabelas
12. finalizar com a task concluída e os links de commit, branch e PR

## Evidências Esperadas na Entrega

- README explicando a solução
- desenho macro da arquitetura
- scripts de infraestrutura
- pipeline YAML
- artefatos de build
- testes executados
- deploy em nuvem
- CRUD com persistência validada no banco
