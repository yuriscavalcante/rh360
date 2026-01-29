import {
  Controller,
  Post,
  Param,
  Body,
  Query,
  UseGuards,
  UseInterceptors,
  UploadedFile,
  HttpStatus,
  HttpException,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { FaceService } from './face.service';
import { FaceVerifyResponse } from './dto/face-verify-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';

@Controller('api/faces')
@UseGuards(JwtAuthGuard)
export class FaceController {
  constructor(private readonly service: FaceService) {}

  @Post(':userId/verify')
  @UseInterceptors(FileInterceptor('photo'))
  async verifyFace(
    @Param('userId') userId: string,
    @UploadedFile() photo: Express.Multer.File,
    @UserId() tokenUserId: string,
  ): Promise<FaceVerifyResponse> {
    if (tokenUserId !== userId) {
      throw new HttpException(
        { error: 'Você não tem permissão para validar a face deste usuário' },
        HttpStatus.FORBIDDEN,
      );
    }

    if (!photo) {
      throw new HttpException(
        { error: 'Arquivo de imagem é obrigatório' },
        HttpStatus.BAD_REQUEST,
      );
    }

    try {
      return this.service.verifyFace(userId, photo);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @Post('me/verify')
  @UseInterceptors(FileInterceptor('photo'))
  async verifyMyFace(
    @UploadedFile() photo: Express.Multer.File,
    @UserId() userId: string,
  ): Promise<FaceVerifyResponse> {
    return this.verifyFace(userId, photo, userId);
  }

  @Post(':userId/register')
  @UseInterceptors(FileInterceptor('photo'))
  async registerFace(
    @Param('userId') userId: string,
    @UploadedFile() photo: Express.Multer.File,
    @UserId() tokenUserId: string,
  ): Promise<{ message: string }> {
    if (tokenUserId !== userId) {
      throw new HttpException(
        { error: 'Você não tem permissão para registrar a face deste usuário' },
        HttpStatus.FORBIDDEN,
      );
    }

    if (!photo) {
      throw new HttpException(
        { error: 'Arquivo de imagem é obrigatório' },
        HttpStatus.BAD_REQUEST,
      );
    }

    try {
      const success = await this.service.addFace(userId, photo);
      if (success) {
        return { message: 'Face registrada com sucesso no CompreFace' };
      } else {
        throw new HttpException(
          { error: 'Falha ao registrar face no CompreFace' },
          HttpStatus.INTERNAL_SERVER_ERROR,
        );
      }
    } catch (e) {
      throw new HttpException(
        { error: `Erro ao processar registro facial: ${e.message}` },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @Post('me/register')
  @UseInterceptors(FileInterceptor('photo'))
  async registerMyFace(
    @UploadedFile() photo: Express.Multer.File,
    @UserId() userId: string,
  ): Promise<{ message: string }> {
    return this.registerFace(userId, photo, userId);
  }

  @Post(':userId/verify-url')
  async verifyFaceFromUrl(
    @Param('userId') userId: string,
    @Body() body: { photoUrl: string },
    @UserId() tokenUserId: string,
  ): Promise<FaceVerifyResponse> {
    if (tokenUserId !== userId) {
      throw new HttpException(
        { error: 'Você não tem permissão para validar a face deste usuário' },
        HttpStatus.FORBIDDEN,
      );
    }

    if (!body.photoUrl) {
      throw new HttpException(
        { error: 'URL da imagem é obrigatória' },
        HttpStatus.BAD_REQUEST,
      );
    }

    try {
      return this.service.verifyFaceFromUrl(userId, body.photoUrl);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @Post(':userId/register-url')
  async registerFaceFromUrl(
    @Param('userId') userId: string,
    @Body() body: { photoUrl: string },
    @UserId() tokenUserId: string,
  ): Promise<{ message: string }> {
    if (tokenUserId !== userId) {
      throw new HttpException(
        { error: 'Você não tem permissão para registrar a face deste usuário' },
        HttpStatus.FORBIDDEN,
      );
    }

    if (!body.photoUrl) {
      throw new HttpException(
        { error: 'URL da imagem é obrigatória' },
        HttpStatus.BAD_REQUEST,
      );
    }

    try {
      const success = await this.service.addFaceFromUrl(userId, body.photoUrl);
      if (success) {
        return { message: 'Face registrada com sucesso no CompreFace' };
      } else {
        throw new HttpException(
          { error: 'Falha ao registrar face no CompreFace' },
          HttpStatus.INTERNAL_SERVER_ERROR,
        );
      }
    } catch (e) {
      throw new HttpException(
        { error: `Erro ao processar registro facial: ${e.message}` },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}
