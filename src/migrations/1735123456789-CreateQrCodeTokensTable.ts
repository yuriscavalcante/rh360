import { MigrationInterface, QueryRunner, Table } from 'typeorm';

export class CreateQrCodeTokensTable1735123456789 implements MigrationInterface {
  public async up(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.createTable(
      new Table({
        name: 'qrcode_tokens',
        columns: [
          {
            name: 'id',
            type: 'int',
            isPrimary: true,
            isGenerated: true,
            generationStrategy: 'increment',
          },
          {
            name: 'token',
            type: 'varchar',
            length: '1000',
            isUnique: true,
            isNullable: false,
          },
          {
            name: 'user_id',
            type: 'uuid',
            isNullable: false,
          },
          {
            name: 'active',
            type: 'boolean',
            isNullable: false,
            default: true,
          },
          {
            name: 'created_at',
            type: 'timestamp',
            default: 'CURRENT_TIMESTAMP',
            isNullable: false,
          },
          {
            name: 'expires_at',
            type: 'timestamp',
            isNullable: false,
          },
        ],
        indices: [
          {
            name: 'IDX_qrcode_tokens_user_id',
            columnNames: ['user_id'],
          },
          {
            name: 'IDX_qrcode_tokens_active',
            columnNames: ['active'],
          },
          {
            name: 'IDX_qrcode_tokens_expires_at',
            columnNames: ['expires_at'],
          },
        ],
      }),
      true,
    );
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.dropTable('qrcode_tokens');
  }
}
