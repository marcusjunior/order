# Arquitetura da Solução - Order Management System

## Visão Geral da Arquitetura

### Diagrama de Componentes

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
│                       ORDER MANAGEMENT SERVICE                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                      Controller Layer                            │  │
│  │  - OrderController (REST API)                                    │  │
│  │  - Validação de entrada                                          │  │
│  │  - Paginação                                                      │  │
│  └──────────────────┬───────────────────────────────────────────────┘  │
│                     ▼                                                   │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                      Service Layer                               │  │
│  │  - OrderService (Lógica de negócio)                             │  │
│  │  - OrderConsumerService (RabbitMQ Consumer)                      │  │
│  │  - OrderPublisherService (RabbitMQ Publisher)                    │  │
│  │                                                                   │  │
│  │  Funcionalidades:                                                │  │
│  │  ✓ Verificação de duplicação (Redis + DB)                       │  │
│  │  ✓ Cálculo de valor total                                       │  │
│  │  ✓ Transações ACID                                              │  │
│  │  ✓ Optimistic Locking                                           │  │
│  │  ✓ Cache de consultas                                           │  │
│  └──────────────────┬───────────────────────────────────────────────┘  │
│                     ▼                                                   │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    Repository Layer                              │  │
│  │  - OrderRepository (Spring Data JPA)                            │  │
│  │  - Queries otimizadas                                           │  │
│  │  - Fetch joins                                                   │  │
│  └──────────────────┬───────────────────────────────────────────────┘  │
└────────────────────┼────────────────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┬────────────────┬─────────────┐
        ▼            ▼            ▼                ▼             ▼
   ┌─────────┐  ┌─────────┐  ┌─────────┐    ┌──────────┐  ┌──────────┐
   │ Redis   │  │PostgreSQL│  │RabbitMQ │    │Prometheus│  │  Logs    │
   │         │  │         │  │         │    │          │  │          │
   │ Cache   │  │ ACID    │  │ Queue   │    │ Metrics  │  │Structured│
   │ Dedup   │  │ Data    │  │ Async   │    │          │  │          │
   └─────────┘  └─────────┘  └────┬────┘    └──────────┘  └──────────┘
                                   │
                                   │ Queue: order.outgoing.queue
                                   ▼
                      ┌────────────────────────────┐
                      │   SISTEMA EXTERNO B        │
                      │   (Consumidor de Pedidos)  │
                      └────────────────────────────┘
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