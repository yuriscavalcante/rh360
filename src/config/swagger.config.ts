import { DocumentBuilder } from '@nestjs/swagger';
import { INestApplication } from '@nestjs/common';
import { SwaggerModule } from '@nestjs/swagger';

const SWAGGER_PATH = 'api-docs';

/**
 * Tags da API na ordem em que aparecem na documentação.
 * Cada tag pode ter uma descrição para o Swagger.
 */
export const SWAGGER_TAGS = [
  { name: 'Health', description: 'Verificação de saúde da aplicação' },
  { name: 'Autenticação', description: 'Login, logout e validação de token' },
  { name: 'Usuários', description: 'Cadastro e gestão de usuários' },
  { name: 'Equipes', description: 'Gestão de equipes' },
  { name: 'Ponto', description: 'Registro de ponto e QR Code' },
  { name: 'Reconhecimento Facial', description: 'Registro e verificação facial' },
  { name: 'Permissões', description: 'Permissões de usuários' },
  { name: 'Templates de Permissão', description: 'Modelos de permissão' },
  { name: 'Tarefas', description: 'Gestão de tarefas' },
  { name: 'Financeiro - Resumo', description: 'Resumo financeiro' },
  { name: 'Financeiro - Despesas', description: 'Despesas pessoais' },
  { name: 'Financeiro - Salários', description: 'Folha de pagamento' },
  { name: 'Financeiro - Despesas da Empresa', description: 'Despesas corporativas' },
  { name: 'Utilitários', description: 'Endpoints auxiliares' },
] as const;

function buildDocumentConfig() {
  const config = new DocumentBuilder()
    .setTitle('RH360 API')
    .setDescription(
      'API REST do sistema RH360 — gestão de pessoas, ponto, reconhecimento facial e financeiro. ' +
        'A maioria dos endpoints exige autenticação via Bearer Token (JWT) no header `Authorization`.',
    )
    .setVersion('1.0')
    .setContact('RH360', undefined, undefined)
    .addBearerAuth(
      {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
        name: 'Authorization',
        description: 'Token JWT obtido no endpoint POST /api/auth/login',
        in: 'header',
      },
      'bearer',
    )
    .addTag(
      SWAGGER_TAGS[0].name,
      SWAGGER_TAGS[0].description,
    )
    .addTag(
      SWAGGER_TAGS[1].name,
      SWAGGER_TAGS[1].description,
    )
    .addTag(
      SWAGGER_TAGS[2].name,
      SWAGGER_TAGS[2].description,
    )
    .addTag(
      SWAGGER_TAGS[3].name,
      SWAGGER_TAGS[3].description,
    )
    .addTag(
      SWAGGER_TAGS[4].name,
      SWAGGER_TAGS[4].description,
    )
    .addTag(
      SWAGGER_TAGS[5].name,
      SWAGGER_TAGS[5].description,
    )
    .addTag(
      SWAGGER_TAGS[6].name,
      SWAGGER_TAGS[6].description,
    )
    .addTag(
      SWAGGER_TAGS[7].name,
      SWAGGER_TAGS[7].description,
    )
    .addTag(
      SWAGGER_TAGS[8].name,
      SWAGGER_TAGS[8].description,
    )
    .addTag(
      SWAGGER_TAGS[9].name,
      SWAGGER_TAGS[9].description,
    )
    .addTag(
      SWAGGER_TAGS[10].name,
      SWAGGER_TAGS[10].description,
    )
    .addTag(
      SWAGGER_TAGS[11].name,
      SWAGGER_TAGS[11].description,
    )
    .addTag(
      SWAGGER_TAGS[12].name,
      SWAGGER_TAGS[12].description,
    )
    .addTag(
      SWAGGER_TAGS[13].name,
      SWAGGER_TAGS[13].description,
    );

  return config.build();
}

/**
 * Opções customizadas do Swagger UI (ordenação, título, etc.).
 */
export const swaggerCustomOptions = {
  customSiteTitle: 'RH360 — Documentação da API',
  customfavIcon: undefined,
  customCss: `
    .swagger-ui .topbar { background-color: #1e3a5f; }
    .swagger-ui .info .title { font-size: 1.8em; }
  `,
  swaggerOptions: {
    persistAuthorization: true,
    docExpansion: 'list' as const,
    filter: true,
    showRequestDuration: true,
    tagsSorter: 'alpha',
    operationsSorter: 'alpha',
  },
};

/**
 * Configura e registra o Swagger na aplicação NestJS.
 */
export function setupSwagger(app: INestApplication): void {
  const config = buildDocumentConfig();
  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup(SWAGGER_PATH, app, document, swaggerCustomOptions);
}

export { SWAGGER_PATH };
