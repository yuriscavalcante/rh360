import {
  Controller,
  Post,
  Param,
  Body,
  UseGuards,
  UseInterceptors,
  UploadedFile,
  HttpStatus,
  HttpException,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse, ApiBody, ApiParam } from '@nestjs/swagger';
import { FaceService } from './face.service';
import { FaceVerifyResponse } from './dto/face-verify-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';

@ApiTags('Reconhecimento Facial')
@Controller('api/faces')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class FaceController {
  constructor(private readonly service: FaceService) {}

  @ApiOperation({ summary: 'Verificar face do usuário (upload de foto)' })
  @ApiParam({ name: 'userId', description: 'ID do usuário' })
  @ApiResponse({ status: 200, description: 'Verificação realizada', type: FaceVerifyResponse })
  @ApiResponse({ status: 403, description: 'Sem permissão para validar este usuário' })
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

  @ApiOperation({ summary: 'Verificar minha face (upload de foto)' })
  @ApiResponse({ status: 200, description: 'Verificação realizada', type: FaceVerifyResponse })
  @Post('me/verify')
  @UseInterceptors(FileInterceptor('photo'))
  async verifyMyFace(
    @UploadedFile() photo: Express.Multer.File,
    @UserId() userId: string,
  ): Promise<FaceVerifyResponse> {
    return this.verifyFace(userId, photo, userId);
  }

  @ApiOperation({ summary: 'Registrar face do usuário (upload de foto)' })
  @ApiParam({ name: 'userId', description: 'ID do usuário' })
  @ApiResponse({ status: 200, description: 'Face registrada com sucesso' })
  @ApiResponse({ status: 403, description: 'Sem permissão' })
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

  @ApiOperation({ summary: 'Registrar minha face (upload de foto)' })
  @ApiResponse({ status: 200, description: 'Face registrada com sucesso' })
  @Post('me/register')
  @UseInterceptors(FileInterceptor('photo'))
  async registerMyFace(
    @UploadedFile() photo: Express.Multer.File,
    @UserId() userId: string,
  ): Promise<{ message: string }> {
    return this.registerFace(userId, photo, userId);
  }

  @ApiOperation({ summary: 'Verificar face por URL da foto' })
  @ApiParam({ name: 'userId', description: 'ID do usuário' })
  @ApiBody({ schema: { type: 'object', properties: { photoUrl: { type: 'string' } }, required: ['photoUrl'] } })
  @ApiResponse({ status: 200, description: 'Verificação realizada', type: FaceVerifyResponse })
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

  @ApiOperation({ summary: 'Registrar face por URL da foto' })
  @ApiParam({ name: 'userId', description: 'ID do usuário' })
  @ApiBody({ schema: { type: 'object', properties: { photoUrl: { type: 'string' } }, required: ['photoUrl'] } })
  @ApiResponse({ status: 200, description: 'Face registrada com sucesso' })
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
