import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { TimeClock } from '../entities/time-clock.entity';
import { User } from '../entities/user.entity';

@Injectable()
export class TimeClockService {
  constructor(
    @InjectRepository(TimeClock)
    private timeClockRepository: Repository<TimeClock>,
    @InjectRepository(User)
    private usersRepository: Repository<User>,
  ) {}

  async create(userId: string, message?: string): Promise<TimeClock> {
    const user = await this.usersRepository.findOne({ where: { id: userId } });

    if (!user) {
      throw new Error('Usuário não encontrado');
    }

    const timeClock = this.timeClockRepository.create({
      user,
      timestamp: new Date(),
      message,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    });

    return this.timeClockRepository.save(timeClock);
  }

  async findByUser(userId: string): Promise<TimeClock[]> {
    return this.timeClockRepository.find({
      where: { user: { id: userId } },
      order: { timestamp: 'DESC' },
    });
  }

  async findAll(): Promise<TimeClock[]> {
    return this.timeClockRepository.find({
      relations: ['user'],
      order: { timestamp: 'DESC' },
    });
  }
}
