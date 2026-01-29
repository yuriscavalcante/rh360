import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
} from 'typeorm';

@Entity('qrcode_tokens')
export class QrCodeToken {
  @PrimaryGeneratedColumn('increment')
  id: number;

  @Column({ length: 1000, unique: true, nullable: false })
  token: string;

  @Column('uuid', { name: 'user_id', nullable: false })
  userId: string;

  @Column({ nullable: false, default: true })
  active: boolean;

  @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
  createdAt: Date;

  @Column({ name: 'expires_at', type: 'timestamp', nullable: false })
  expiresAt: Date;
}
