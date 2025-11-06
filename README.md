# Order Management System

Sistema de gerenciamento de pedidos com alta volumetria (150k-200k pedidos/dia).

## Arquitetura

### Componentes

- **API REST**: Recebimento de pedidos via HTTP (Sistema Externo A)
- **RabbitMQ Consumer**: Recebimento de pedidos via fila assíncrona
- **PostgreSQL**: Armazenamento persistente com índices otimizados
- **Redis**: Cache de consultas e verificação de duplicação
- **RabbitMQ Publisher**: Envio de pedidos processados (Sistema Externo B)

### Fluxo de Processamento

1. Sistema Externo A envia pedidos via REST API ou RabbitMQ
2. Verificação de duplicação (Redis + PostgreSQL)
3. Validação dos dados de entrada
4. Cálculo do valor total do pedido
5. Persistência no banco de dados (transacional)
6. Publicação para Sistema Externo B via RabbitMQ
7. Cache da consulta para otimização

## Requisitos

- Java 17
- Docker e Docker Compose
- Gradle

## Executando o Projeto

### 1. Iniciar Infraestrutura

```bash
docker-compose up -d
```

Aguarde os serviços iniciarem:
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- RabbitMQ: localhost:5672
- RabbitMQ Management: http://localhost:15672 (guest/guest)

### 2. Executar Aplicação

```bash
gradlew bootRun
```

ou no Windows:

```cmd
gradlew.bat bootRun
```

A aplicação estará disponível em: http://localhost:8080

## Endpoints da API

### Criar Pedido (Sistema Externo A)

```http
POST /api/v1/orders
Content-Type: application/json

{
  "externalId": "ORDER-12345",
  "items": [
    {
      "productCode": "PROD-001",
      "quantity": 2,
      "unitPrice": 50.00
    },
    {
      "productCode": "PROD-002",
      "quantity": 1,
      "unitPrice": 30.00
    }
  ]
}
```

Resposta:
```json
{
  "id": "uuid",
  "externalId": "ORDER-12345",
  "status": "COMPLETED",
  "totalAmount": 130.00,
  "createdAt": "2025-10-27T10:00:00",
  "updatedAt": "2025-10-27T10:00:00",
  "items": [...]
}
```

### Consultar Pedido por ID (Sistema Externo B)

```http
GET /api/v1/orders/{id}
```

### Consultar Pedido por External ID

```http
GET /api/v1/orders/external/{externalId}
```

### Listar Todos os Pedidos (Paginado)

```http
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
```

### Listar Pedidos por Status

```http
GET /api/v1/orders/status/COMPLETED?page=0&size=20
```

Status disponíveis: RECEIVED, PROCESSING, COMPLETED, FAILED

## Monitoramento

### Métricas Prometheus

```http
GET /actuator/prometheus
GET /actuator/metrics
```

### Health Check

```http
GET /actuator/health
```

## Testes

### Executar Testes

```bash
gradlew test
```

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA** - Persistência
- **PostgreSQL** - Banco de dados relacional
- **Redis** - Cache e deduplicação
- **RabbitMQ** - Mensageria assíncrona
- **Flyway** - Migração de banco de dados
- **MapStruct** - Mapeamento de objetos
- **Lombok** - Redução de boilerplate
- **Resilience4j** - Circuit breaker e retry
- **Micrometer/Prometheus** - Métricas
- **Testcontainers** - Testes de integração

### Otimizações Implementadas

1. **Batch Processing**: Hibernate batch size de 50
2. **Connection Pool**: HikariCP configurado
3. **Índices**: Cobertura completa nas consultas
4. **Cache**: Redis com TTL de 1h para consultas
5. **Assíncrono**: RabbitMQ para desacoplamento
6. **Paginação**: Todas as listagens são paginadas

### Escalabilidade Horizontal

- Múltiplas instâncias da aplicação podem ser executadas
- RabbitMQ distribui mensagens entre consumidores
- Redis compartilhado entre instâncias
- PostgreSQL pode ser escalado com read replicas

## Arquitetura Final

```
┌─────────────────┐
│  Sistema A      │
│  (Externo)      │
└────────┬────────┘
         │
         ▼
    ┌────────┐      ┌──────────┐
    │ REST   │      │ RabbitMQ │
    │ API    │◄─────┤ Consumer │
    └───┬────┘      └──────────┘
        │
        ▼
    ┌─────────────────────┐
    │  OrderService       │
    │  - Deduplication    │
    │  - Validation       │
    │  - Calculation      │
    └──────┬──────────────┘
           │
    ┌──────┴──────┬───────────┬─────────┐
    ▼             ▼           ▼         ▼
┌──────┐    ┌──────────┐  ┌──────┐  ┌──────────┐
│Redis │    │PostgreSQL│  │Cache │  │ RabbitMQ │
│(TTL) │    │(ACID)    │  │      │  │Publisher │
└──────┘    └──────────┘  └──────┘  └─────┬────┘
                                           │
                                           ▼
                                    ┌─────────────┐
                                    │  Sistema B  │
                                    │  (Externo)  │
                                    └─────────────┘
```
