# Arquitetura da Solução - Order Management System

## Arquitetura Hexagonal (Ports and Adapters)

### Visão Geral
Este projeto segue a **Arquitetura Hexagonal**, também conhecida como **Ports and Adapters**, que promove:
- **Isolamento do domínio** de detalhes de infraestrutura
- **Testabilidade** através de interfaces bem definidas
- **Flexibilidade** para trocar tecnologias sem afetar a lógica de negócio
- **Separação clara** entre lógica de negócio e mecanismos técnicos

### Estrutura de Camadas

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          SISTEMA EXTERNO A                              │
│                     (Fornecedor de Pedidos)                             │
└────────────┬────────────────────────────────┬───────────────────────────┘
             │                                │
             │ REST API                       │ RabbitMQ
             │ POST /api/v1/orders           │ Queue: order.incoming.queue
             │                                │
             ▼                                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    ADAPTERS IN (Driving Adapters)                      │
│  ┌──────────────────────────┐    ┌──────────────────────────────┐    │
│  │  OrderRestAdapter        │    │ RabbitMQConsumerAdapter      │    │
│  │  (REST Controller)       │    │  (Message Consumer)          │    │
│  └────────────┬─────────────┘    └────────────┬─────────────────┘    │
└───────────────┼──────────────────────────────┼──────────────────────────┘
                │                              │
                └──────────────┬───────────────┘
                               ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         DOMAIN CORE (Hexagon)                          │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │                    PORTS IN (Use Cases)                          │ │
│  │  - CreateOrderUseCase                                            │ │
│  │  - QueryOrderUseCase                                             │ │
│  └──────────────────┬───────────────────────────────────────────────┘ │
│                     ▼                                                  │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │                   DOMAIN SERVICES                                │ │
│  │  - CreateOrderService (Business Logic)                          │ │
│  │  - QueryOrderService (Query Logic)                              │ │
│  │                                                                   │ │
│  │  Regras de Negócio:                                             │ │
│  │  ✓ Verificação de duplicação (Redis + DB)                       │ │
│  │  ✓ Cálculo de valor total                                       │ │
│  │  ✓ Validação de pedidos                                         │ │
│  │  ✓ Mudança de status                                            │ │
│  │  ✓ Publicação para sistema externo                              │ │
│  └──────────────────┬───────────────────────────────────────────────┘ │
│                     ▼                                                  │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │                    DOMAIN MODEL                                  │ │
│  │  - OrderDomain (Pure domain entity)                             │ │
│  │  - OrderItemDomain                                               │ │
│  │  - OrderStatusDomain                                             │ │
│  └──────────────────┬───────────────────────────────────────────────┘ │
│                     ▼                                                  │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │                   PORTS OUT (Interfaces)                         │ │
│  │  - OrderRepositoryPort                                           │ │
│  │  - OrderCachePort                                                │ │
│  │  - OrderPublisherPort                                            │ │
│  └──────────────────┬───────────────────────────────────────────────┘ │
└────────────────────┼────────────────────────────────────────────────────┘
                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   ADAPTERS OUT (Driven Adapters)                       │
│  ┌──────────────────┐  ┌──────────────┐  ┌─────────────────────┐    │
│  │ Persistence      │  │ Cache        │  │ Messaging           │    │
│  │ Adapter          │  │ Adapter      │  │ Adapter             │    │
│  │ (JPA/Postgres)   │  │ (Redis)      │  │ (RabbitMQ)          │    │
│  └────────┬─────────┘  └──────┬───────┘  └──────┬──────────────┘    │
└───────────┼────────────────────┼──────────────────┼───────────────────┘
            ▼                    ▼                  ▼
   ┌──────────────┐      ┌──────────┐      ┌─────────────┐
   │ PostgreSQL   │      │  Redis   │      │  RabbitMQ   │
   │ (Database)   │      │ (Cache)  │      │  (Queue)    │
   └──────────────┘      └──────────┘      └──────┬──────┘
                                                   │
                                                   │ Queue: order.outgoing.queue
                                                   ▼
                                      ┌────────────────────────────┐
                                      │   SISTEMA EXTERNO B        │
                                      │   (Consumidor de Pedidos)  │
                                      └────────────────────────────┘
```

### Estrutura de Pacotes

```
com.ambev.order/
├── domain/                          # CORE - Regras de Negócio
│   ├── model/                       # Domain Models (Pure POJOs)
│   │   ├── OrderDomain
│   │   ├── OrderItemDomain
│   │   └── OrderStatusDomain
│   ├── port/
│   │   ├── in/                      # Input Ports (Use Cases)
│   │   │   ├── CreateOrderUseCase
│   │   │   └── QueryOrderUseCase
│   │   └── out/                     # Output Ports (Interfaces)
│   │       ├── OrderRepositoryPort
│   │       ├── OrderCachePort
│   │       └── OrderPublisherPort
│   └── service/                     # Domain Services
│       ├── CreateOrderService
│       └── QueryOrderService
├── adapter/                         # ADAPTERS
│   ├── in/                          # Driving Adapters (Primary)
│   │   ├── rest/
│   │   │   └── OrderRestAdapter
│   │   └── messaging/
│   │       └── RabbitMQConsumerAdapter
│   └── out/                         # Driven Adapters (Secondary)
│       ├── persistence/
│       │   ├── OrderJpaRepository
│       │   ├── OrderPersistenceAdapter
│       │   └── OrderPersistenceMapper
│       ├── cache/
│       │   └── RedisCacheAdapter
│       └── messaging/
│           └── RabbitMQPublisherAdapter
├── dto/                             # Data Transfer Objects
├── mapper/                          # Mappers (DTO ↔ Domain)
├── exception/                       # Exceptions
└── config/                          # Configuration
```

## Fluxo de Processamento Detalhado

### 1. Recebimento do Pedido

```
Sistema A → [REST API / RabbitMQ] → OrderController/OrderConsumer
                                           ↓
                                    Validação (Bean Validation)
                                           ↓
                                    OrderService.createOrder()
```

### 2. Verificação de Duplicação (Dupla Camada)

```
OrderService.isDuplicate()
      ↓
┌─────────────────┐
│  1. Redis Check │  ← Verificação rápida em memória (< 1ms)
│  Key: order:duplicate:{externalId}                        │
│  TTL: 24 horas  │
└────────┬────────┘
         │ Se não encontrado
         ▼
┌─────────────────┐
│  2. DB Check    │  ← Verificação definitiva (constraint UNIQUE)
│  Query: EXISTS  │
│  by external_id │
└─────────────────┘
```

### 3. Processamento e Cálculo

```
1. Status: RECEIVED
2. Mapear DTOs → Entities
3. Adicionar items ao pedido
4. Status: PROCESSING
5. Calcular total:
   totalAmount = Σ(item.quantity × item.unitPrice)
6. Salvar no banco (transação ACID)
7. Marcar no Redis como processado
8. Status: COMPLETED
9. Publicar para Sistema B
10. Cache da consulta
```

### 4. Consulta de Pedidos (Sistema B)

```
GET /api/v1/orders/{id}
      ↓
┌─────────────────┐
│  1. Cache Check │  ← Redis (TTL: 1 hora)
└────────┬────────┘
         │ Cache miss
         ▼
┌─────────────────┐
│  2. DB Query    │  ← PostgreSQL com JOIN FETCH
│  + Cache Store  │
└─────────────────┘
```