import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
} from 'typeorm';

@Entity('permission_template')
export class PermissionTemplate {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  nome: string;

  @Column()
  label: string;

  @Column()
  rota: string;

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;

  @Column({ type: 'varchar', nullable: true })
  updatedAt: string;

  @Column({ type: 'varchar', nullable: true })
  deletedAt: string;
}
