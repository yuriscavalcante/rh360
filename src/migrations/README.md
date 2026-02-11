# Migrations

Este diretório contém as migrations do banco de dados.

## Executando Migrations

### Executar todas as migrations pendentes:
```bash
npm run migration:run
```

### Reverter a última migration:
```bash
npm run migration:revert
```

### Criar uma nova migration:
```bash
npm run migration:create src/migrations/NomeDaMigration
```

### Gerar migration automaticamente a partir das entidades:
```bash
npm run migration:generate src/migrations/NomeDaMigration
```

## Migration: CreateQrCodeTokensTable

Esta migration cria a tabela `qrcode_tokens` para armazenar tokens QR code usados para validação de ponto.

**Tabela criada:**
- `qrcode_tokens`
  - `id` (int, primary key, auto increment)
  - `token` (varchar 1000, unique, not null)
  - `user_id` (uuid, not null)
  - `active` (boolean, default true, not null)
  - `created_at` (timestamp, default CURRENT_TIMESTAMP)
  - `expires_at` (timestamp, not null)

**Índices criados:**
- `IDX_qrcode_tokens_user_id` - índice em `user_id`
- `IDX_qrcode_tokens_active` - índice em `active`
- `IDX_qrcode_tokens_expires_at` - índice em `expires_at`
