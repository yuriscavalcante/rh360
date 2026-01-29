import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
} from 'typeorm';

@Entity('tokens')
export class Token {
  @PrimaryGeneratedColumn('increment')
  id: number;

  @Column({ length: 1000, unique: true, nullable: false })
  token: string;

  @Column('uuid', { name: 'user_id', nullable: false })
  userId: string;

  @Column({ nullable: false, default: true })
  active: boolean;

  @Column({ name: 'created_at', type: 'timestamp', nullable: false })
  createdAt: Date;

  @Column({ name: 'expires_at', type: 'timestamp', nullable: false })
  expiresAt: Date;
}
