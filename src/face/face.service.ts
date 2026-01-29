import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';
import * as FormData from 'form-data';
import { UsersService } from '../users/users.service';
import { FaceVerifyResponse } from './dto/face-verify-response.dto';

@Injectable()
export class FaceService {
  private readonly comprefaceApiUrl: string;
  private readonly comprefaceApiKey: string;
  private readonly subjectEndpoint: string;
  private readonly verifyEndpoint: string;

  constructor(
    private readonly configService: ConfigService,
    private readonly httpService: HttpService,
    private readonly usersService: UsersService,
  ) {
    this.comprefaceApiUrl =
      this.configService.get<string>('COMPREFACE_API_URL') || 'http://localhost:8000';
    this.comprefaceApiKey = this.configService.get<string>('COMPREFACE_API_KEY') || '';
    this.subjectEndpoint = '/api/v1/recognition/faces';
    this.verifyEndpoint = '/api/v1/verification/verify';
  }

  async verifyFace(
    userId: string,
    photo: Express.Multer.File,
  ): Promise<FaceVerifyResponse> {
    if (!photo || photo.size === 0) {
      throw new Error('Arquivo de imagem é obrigatório');
    }

    if (!this.comprefaceApiKey) {
      throw new Error('API Key do CompreFace não configurada');
    }

    const user = await this.usersService.findById(userId);
    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    try {
      const baseUrl = this.comprefaceApiUrl.endsWith('/')
        ? this.comprefaceApiUrl.slice(0, -1)
        : this.comprefaceApiUrl;
      const url = `${baseUrl}${this.verifyEndpoint}`;

      const formData = new FormData();
      formData.append('file', photo.buffer, {
        filename: photo.originalname,
        contentType: photo.mimetype,
      });

      const response = await firstValueFrom(
        this.httpService.post(url, formData, {
          headers: {
            'x-api-key': this.comprefaceApiKey,
            ...formData.getHeaders(),
          },
        }),
      );

      return this.processVerifyResponse(userId, response.data);
    } catch (error) {
      throw new Error(`Erro ao verificar face no CompreFace: ${error.message}`);
    }
  }

  async verifyFaceFromUrl(
    userId: string,
    photoUrl: string,
  ): Promise<FaceVerifyResponse> {
    if (!photoUrl || photoUrl.trim() === '') {
      throw new Error('URL da imagem é obrigatória');
    }

    if (!this.comprefaceApiKey) {
      throw new Error('API Key do CompreFace não configurada');
    }

    const user = await this.usersService.findById(userId);
    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    try {
      const baseUrl = this.comprefaceApiUrl.endsWith('/')
        ? this.comprefaceApiUrl.slice(0, -1)
        : this.comprefaceApiUrl;
      const url = `${baseUrl}${this.verifyEndpoint}`;

      const response = await firstValueFrom(
        this.httpService.post(
          url,
          { url: photoUrl },
          {
            headers: {
              'x-api-key': this.comprefaceApiKey,
              'Content-Type': 'application/json',
            },
          },
        ),
      );

      return this.processVerifyResponse(userId, response.data);
    } catch (error) {
      throw new Error(`Erro ao verificar face no CompreFace: ${error.message}`);
    }
  }

  async addFace(userId: string, photo: Express.Multer.File): Promise<boolean> {
    if (!photo || photo.size === 0) {
      throw new Error('Arquivo de imagem é obrigatório');
    }

    if (!this.comprefaceApiKey) {
      throw new Error('API Key do CompreFace não configurada');
    }

    const user = await this.usersService.findById(userId);
    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    try {
      const baseUrl = this.comprefaceApiUrl.endsWith('/')
        ? this.comprefaceApiUrl.slice(0, -1)
        : this.comprefaceApiUrl;
      const url = `${baseUrl}${this.subjectEndpoint}`;

      const formData = new FormData();
      formData.append('file', photo.buffer, {
        filename: photo.originalname,
        contentType: photo.mimetype,
      });
      formData.append('subject', userId);

      const response = await firstValueFrom(
        this.httpService.post(url, formData, {
          headers: {
            'x-api-key': this.comprefaceApiKey,
            ...formData.getHeaders(),
          },
        }),
      );

      return response.status === 200 || response.status === 201;
    } catch (error) {
      return false;
    }
  }

  async addFaceFromUrl(userId: string, photoUrl: string): Promise<boolean> {
    if (!photoUrl || photoUrl.trim() === '') {
      throw new Error('URL da imagem é obrigatória');
    }

    if (!this.comprefaceApiKey) {
      throw new Error('API Key do CompreFace não configurada');
    }

    const user = await this.usersService.findById(userId);
    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    try {
      const baseUrl = this.comprefaceApiUrl.endsWith('/')
        ? this.comprefaceApiUrl.slice(0, -1)
        : this.comprefaceApiUrl;
      const url = `${baseUrl}${this.subjectEndpoint}`;

      const response = await firstValueFrom(
        this.httpService.post(
          url,
          { url: photoUrl, subject: userId },
          {
            headers: {
              'x-api-key': this.comprefaceApiKey,
              'Content-Type': 'application/json',
            },
          },
        ),
      );

      return response.status === 200 || response.status === 201;
    } catch (error) {
      return false;
    }
  }

  private processVerifyResponse(
    userId: string,
    responseData: any,
  ): FaceVerifyResponse {
    const expectedSubject = userId;
    let verified = false;
    let maxConfidence = 0.0;
    let foundSubject: string | null = null;

    // Processar resposta do CompreFace
    if (responseData.result && Array.isArray(responseData.result)) {
      for (const result of responseData.result) {
        if (result.subjects && Array.isArray(result.subjects)) {
          for (const subject of result.subjects) {
            const subjectStr = subject.subject?.toString().trim();
            const similarity = subject.similarity || 0;

            if (subjectStr && subjectStr === expectedSubject) {
              verified = true;
              foundSubject = subjectStr;
              maxConfidence = similarity;
              break;
            }

            if (similarity > maxConfidence) {
              maxConfidence = similarity;
              foundSubject = subjectStr || null;
            }
          }
        }
      }
    }

    return {
      verified,
      confidence: maxConfidence,
      message: verified
        ? `Face verificada com sucesso (confiança: ${(maxConfidence * 100).toFixed(2)}%)`
        : `Face não corresponde. Subject encontrado: ${foundSubject || 'nenhum'}, Confiança: ${(maxConfidence * 100).toFixed(2)}%`,
      user_id: userId,
    };
  }
}
