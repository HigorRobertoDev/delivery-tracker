# Delivery Tracker

Sistema simplificado de rastreamento de pedidos de delivery.

## Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security + JWT, JPA, SQLite
- **Frontend:** React 18, Vite, React Router

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 18+

## Como executar

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

API disponível em `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

O banco SQLite (`delivery.db`) é criado automaticamente na pasta `backend`.

### 2. Frontend

```bash
cd frontend
npm install // Caso apresente problemas, ajustar o comando para npm install --legacy-peer-deps
npm run dev
```

Aplicação disponível em `http://localhost:5173`.

## Endpoints

### Autenticação

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/register` | Cadastro (nome, e-mail, senha) |
| POST | `/api/auth/login` | Login (e-mail, senha) |

### Pedidos (requer `Authorization: Bearer <token>`)

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/orders` | Criar pedido |
| GET | `/api/orders` | Listar pedidos |
| GET | `/api/orders/{id}` | Buscar pedido por ID |
| PUT | `/api/orders/{id}/status` | Atualizar status |

### Status possíveis

`RECEBIDO` → `EM_PREPARO` → `SAIU_PARA_ENTREGA` → `ENTREGUE`  
Também: `CANCELADO`

### Exemplos

**Cadastro**

```json
POST /api/auth/register
{
  "name": "Ana Silva",
  "email": "ana@email.com",
  "password": "senha123"
}
```

**Criar pedido**

```json
POST /api/orders
Authorization: Bearer <token>
{
  "customerName": "João",
  "deliveryAddress": "Rua das Flores, 100",
  "items": [
    { "name": "Pizza Margherita", "quantity": 1 },
    { "name": "Refrigerante", "quantity": 2 }
  ]
}
```

**Atualizar status**

```json
PUT /api/orders/1/status
Authorization: Bearer <token>
{
  "status": "EM_PREPARO"
}
```

## Funcionalidades do front-end

- Cadastro e login
- Listagem de pedidos com status atual
- Criação de novo pedido (cliente, endereço e itens)
- Atualização de status diretamente na lista
