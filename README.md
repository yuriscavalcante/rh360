# RH360 - NestJS API

API do sistema RH360 migrado de Spring Boot para NestJS com TypeORM.

## Tecnologias

- NestJS
- TypeORM
- PostgreSQL
- JWT Authentication
- Swagger/OpenAPI
- AWS SDK (Cloudflare R2)

## Pré-requisitos

- Node.js 18+ 
- PostgreSQL 15+
- npm ou yarn

## Instalação

```bash
# Instalar dependências
npm install

# Copiar arquivo de configuração
cp env.example .env

# Editar .env com suas configurações
```

## Configuração do Banco de Dados

O projeto usa PostgreSQL. Você precisa ter um banco PostgreSQL já configurado e rodando. Configure as credenciais no arquivo `.env`:

```env
DB_HOST=localhost  # ou o IP/hostname do seu banco
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_DATABASE=rh360
DB_SYNCHRONIZE=false  # Use false em produção
```

## Executar

### Desenvolvimento Local

```bash
# Instalar dependências
npm install

# Copiar arquivo de configuração
cp env.example .env

# Editar .env com suas configurações

# Executar em modo desenvolvimento
npm run start:dev
```

### Produção com Docker

```bash
# Copiar arquivo de configuração
cp env.example .env

# Editar .env com suas configurações (especialmente DB_HOST para apontar para seu banco externo)

# Build e executar com Docker Compose
docker-compose up -d --build

# Ver logs
docker-compose logs -f app

# Parar a aplicação
docker-compose down
```

### Produção Local

```bash
# Build
npm run build

# Executar
npm run start:prod
```

## Estrutura do Projeto

```
src/
├── entities/          # Entidades TypeORM
├── auth/              # Módulo de autenticação
├── users/              # Módulo de usuários
├── teams/             # Módulo de equipes
├── time-clock/         # Módulo de ponto
├── permissions/        # Módulo de permissões
├── token/              # Serviço de tokens
├── database/           # Configuração do banco
└── main.ts             # Arquivo principal
```

## Documentação

Acesse a documentação Swagger em: `http://localhost:8080/api-docs`

## Endpoints Principais

- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/validate` - Validar token
- `GET /api/users` - Listar usuários
- `POST /api/users` - Criar usuário
- `GET /api/users/me` - Usuário atual
- `POST /api/time-clock` - Bater ponto
- `GET /api/teams` - Listar equipes

## Migração do Spring Boot

Este projeto foi migrado do Spring Boot (Java) para NestJS (TypeScript). As principais mudanças:

- **Framework**: Spring Boot → NestJS
- **ORM**: JPA/Hibernate → TypeORM
- **Linguagem**: Java → TypeScript
- **Autenticação**: Mantida com JWT
- **Estrutura**: Mantida similar com módulos

## Desenvolvimento

```bash
# Executar testes
npm test

# Linting
npm run lint

# Formatação
npm run format
```
