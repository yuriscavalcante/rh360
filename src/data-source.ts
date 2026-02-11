import { DataSource } from 'typeorm';
import { config } from 'dotenv';
import { resolve } from 'path';

// Carregar variáveis de ambiente do arquivo .env
// Tenta carregar do diretório raiz do projeto
const envPath = resolve(process.cwd(), '.env');
config({ path: envPath });

export default new DataSource({
  type: 'postgres',
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '5432', 10),
  username: process.env.DB_USERNAME || 'postgres',
  password: process.env.DB_PASSWORD || 'postgres',
  database: process.env.DB_DATABASE || 'rh360',
  entities: ['src/entities/**/*.entity.ts'],
  migrations: ['src/migrations/**/*.ts'],
  synchronize: false,
  logging: true,
});
