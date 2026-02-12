import { Injectable, HttpException, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { randomUUID } from 'crypto';
import { TimeClock } from '../entities/time-clock.entity';
import { User } from '../entities/user.entity';
import { FaceService } from '../face/face.service';
import { TimeClockGateway } from './time-clock.gateway';

@Injectable()
export class TimeClockService {
  constructor(
    @InjectRepository(TimeClock)
    private timeClockRepository: Repository<TimeClock>,
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    private faceService: FaceService,
    private timeClockGateway: TimeClockGateway,
  ) {}

  async create(userId: string, message?: string): Promise<TimeClock> {
    const user = await this.usersRepository.findOne({ where: { id: userId } });

    if (!user) {
      throw new Error('Usuário não encontrado');
    }

    const timeClock = this.timeClockRepository.create({
      id: randomUUID(),
      user,
      timestamp: new Date(),
      message,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    });

    const saved = await this.timeClockRepository.save(timeClock);
    this.timeClockGateway.emitAttendanceCreated(saved);
    return saved;
  }

  async createWithFaceVerification(
    userId: string,
    photo: Express.Multer.File,
    message?: string,
  ): Promise<TimeClock> {
    // Validar rosto antes de bater o ponto
    const faceVerification = await this.faceService.verifyFace(userId, photo);

    if (!faceVerification.verified) {
      throw new HttpException(
        {
          error: 'Validação facial falhou',
          details: faceVerification.message,
          confidence: faceVerification.confidence,
          userId: userId,
        },
        HttpStatus.FORBIDDEN,
      );
    }

    // Se a validação passou, criar o registro de ponto
    return this.create(userId, message);
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
