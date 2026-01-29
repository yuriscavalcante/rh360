import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Query,
  UseGuards,
  UseInterceptors,
  UploadedFile,
  HttpException,
  HttpStatus,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiConsumes,
  ApiBody,
  ApiParam,
  ApiQuery,
  ApiResponse,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { QrCodeTokenGuard } from '../auth/guards/qrcode-token.guard';
import { UserId, UserEmail, UserRole } from '../auth/decorators/user.decorator';
import { TimeClockService } from './time-clock.service';
import { QrCodeService } from './qrcode.service';
import { QrCodeTokenService } from '../qrcode-token/qrcode-token.service';
import { UsersService } from '../users/users.service';
import { TimeClock } from '../entities/time-clock.entity';
import { QrCodeResponse } from './dto/qrcode-response.dto';

@ApiTags('Ponto')
@Controller('api/time-clock')
export class TimeClockController {
  constructor(
    private readonly timeClockService: TimeClockService,
    private readonly qrCodeService: QrCodeService,
    private readonly qrCodeTokenService: QrCodeTokenService,
    private readonly usersService: UsersService,
  ) {}

  @ApiOperation({ summary: 'Bater ponto (com validação facial opcional)' })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @ApiConsumes('multipart/form-data', 'application/json')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        photo: {
          type: 'string',
          format: 'binary',
          description: 'Arquivo de imagem para validação facial (opcional)',
        },
        message: {
          type: 'string',
          nullable: true,
        },
      },
    },
  })
  @Post()
  @UseInterceptors(FileInterceptor('photo'))
  async create(
    @UserId() userId: string,
    @UploadedFile() photo?: Express.Multer.File,
    @Body('message') message?: string,
  ): Promise<TimeClock> {
    // Se tiver foto, valida facial antes de bater o ponto
    if (photo) {
      return this.timeClockService.createWithFaceVerification(userId, photo, message);
    }
    
    // Se não tiver foto, bate ponto normalmente
    return this.timeClockService.create(userId, message);
  }

  @ApiOperation({ summary: 'Listar registros de ponto do usuário atual' })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @Get('me')
  async findByUser(@UserId() userId: string): Promise<TimeClock[]> {
    return this.timeClockService.findByUser(userId);
  }

  @ApiOperation({ summary: 'Listar todos os registros de ponto' })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @Get()
  async findAll(): Promise<TimeClock[]> {
    return this.timeClockService.findAll();
  }

  @ApiOperation({
    summary: 'Gerar QR code para bater ponto via mobile',
    description:
      'Gera um QR code com um token temporário que permite ao usuário bater ponto através do celular. ' +
      'O QR code contém uma URL que abre a câmera do celular para capturar a foto e bater o ponto. ' +
      'O token do QR code expira em 15 minutos por segurança. ' +
      'Aceita parâmetros opcionais: "id" (ID customizado) e "path" (caminho para redirecionamento).',
  })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @ApiQuery({
    name: 'id',
    required: false,
    description: 'ID customizado para incluir na URL do QR code',
  })
  @ApiQuery({
    name: 'path',
    required: false,
    description: 'Caminho customizado para redirecionamento',
  })
  @ApiResponse({
    status: 200,
    description: 'QR code gerado com sucesso',
    type: QrCodeResponse,
  })
  @ApiResponse({
    status: 401,
    description: 'Não autenticado - token JWT ausente ou inválido',
  })
  @ApiResponse({
    status: 404,
    description: 'Usuário não encontrado',
  })
  @ApiResponse({
    status: 500,
    description: 'Erro interno do servidor ao gerar QR code',
  })
  @Get('qr-code')
  async generateQrCode(
    @UserId() userId: string,
    @UserEmail() email: string,
    @UserRole() role: string,
    @Query('id') id?: string,
    @Query('path') path?: string,
  ): Promise<QrCodeResponse> {
    try {
      // Verificar se o usuário existe
      await this.usersService.findById(userId);

      // Gerar token temporário para QR code
      const qrToken = await this.qrCodeTokenService.generateQrCodeToken(
        userId,
        email,
        role || 'user',
      );
      await this.qrCodeTokenService.saveQrCodeToken(qrToken, userId);

      // Gerar URL do QR code
      let qrCodeUrl: string;
      if (id && path) {
        // Usar ID e path customizados
        qrCodeUrl = this.qrCodeService.generateQrCodeUrlWithParams(
          id,
          path,
          qrToken,
        );
      } else {
        // Usar comportamento padrão
        qrCodeUrl = this.qrCodeService.generateQrCodeUrl(qrToken);
      }

      // Gerar QR code em Base64
      const qrCodeBase64 = await this.qrCodeService.generateQrCodeBase64(
        qrCodeUrl,
      );

      // Calcular tempo de expiração em minutos
      const qrCodeExpiration = this.qrCodeTokenService.getQrCodeExpiration();
      const expiresInMinutes = qrCodeExpiration / 60000; // converter ms para minutos

      const response: QrCodeResponse = {
        qrCodeBase64,
        url: qrCodeUrl,
        token: qrToken,
        expiresInMinutes,
      };

      return response;
    } catch (error) {
      if (error instanceof HttpException) {
        throw error;
      }
      if (error?.status === HttpStatus.NOT_FOUND || error?.statusCode === HttpStatus.NOT_FOUND) {
        throw new HttpException(
          'Usuário não encontrado',
          HttpStatus.NOT_FOUND,
        );
      }
      throw new HttpException(
        `Erro ao gerar QR code: ${error?.message || 'Erro desconhecido'}`,
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @ApiOperation({
    summary: 'Bater ponto via mobile',
    description:
      'Endpoint para bater ponto através do celular usando autenticação JWT. ' +
      'A foto será validada usando reconhecimento facial antes de registrar o ponto. ' +
      'Aceita um parâmetro opcional "message" (string) para incluir uma mensagem no registro de ponto.',
  })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        photo: {
          type: 'string',
          format: 'binary',
          description: 'Arquivo de imagem para validação facial (obrigatório)',
        },
        message: {
          type: 'string',
          nullable: true,
          description: 'Mensagem opcional para incluir no registro de ponto',
        },
      },
      required: ['photo'],
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Ponto registrado com sucesso',
    type: TimeClock,
  })
  @ApiResponse({
    status: 400,
    description:
      'Requisição inválida - arquivo de imagem ausente ou face não validada',
  })
  @ApiResponse({
    status: 401,
    description: 'Token JWT inválido ou expirado',
  })
  @ApiResponse({
    status: 500,
    description: 'Erro interno do servidor ou erro na comunicação com CompreFace',
  })
  @ApiOperation({
    summary: 'Bater ponto via mobile usando token QR',
    description:
      'Endpoint para bater ponto através do celular usando token QR code. ' +
      'O token QR deve ser enviado como query parameter "token". ' +
      'A foto será validada usando reconhecimento facial antes de registrar o ponto. ' +
      'Aceita um parâmetro opcional "message" (string) para incluir uma mensagem no registro de ponto. ' +
      'O token QR será desativado após o uso para segurança.',
  })
  @ApiQuery({
    name: 'token',
    required: true,
    description: 'Token QR code para autenticação',
  })
  @UseGuards(QrCodeTokenGuard)
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        photo: {
          type: 'string',
          format: 'binary',
          description: 'Arquivo de imagem para validação facial (obrigatório)',
        },
        message: {
          type: 'string',
          nullable: true,
          description: 'Mensagem opcional para incluir no registro de ponto',
        },
      },
      required: ['photo'],
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Ponto registrado com sucesso',
    type: TimeClock,
  })
  @ApiResponse({
    status: 400,
    description:
      'Requisição inválida - arquivo de imagem ausente ou face não validada',
  })
  @ApiResponse({
    status: 401,
    description: 'Token QR inválido, expirado ou inativo',
  })
  @ApiResponse({
    status: 500,
    description: 'Erro interno do servidor ou erro na comunicação com CompreFace',
  })
  @Post('mobile/qr')
  @UseInterceptors(FileInterceptor('photo'))
  async clockInMobileWithQr(
    @UserId() userId: string,
    @Query('token') qrToken: string,
    @UploadedFile() photo: Express.Multer.File,
    @Body('message') message?: string,
  ): Promise<TimeClock> {
    try {
      // Verificar se o usuário existe
      await this.usersService.findById(userId);

      // Verificar se o arquivo foi enviado
      if (!photo) {
        throw new HttpException(
          'Arquivo de imagem é obrigatório',
          HttpStatus.BAD_REQUEST,
        );
      }

      // Bater ponto (valida a face internamente)
      const timeClock = await this.timeClockService.createWithFaceVerification(
        userId,
        photo,
        message,
      );

      // Desativar o token QR após uso para segurança
      await this.qrCodeTokenService.deactivateQrCodeToken(qrToken);

      return timeClock;
    } catch (error) {
      if (error instanceof HttpException) {
        throw error;
      }
      throw new HttpException(
        `Erro ao bater ponto: ${error.message}`,
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @ApiOperation({
    summary: 'Bater ponto via mobile',
    description:
      'Endpoint para bater ponto através do celular usando autenticação JWT. ' +
      'A foto será validada usando reconhecimento facial antes de registrar o ponto. ' +
      'Aceita um parâmetro opcional "message" (string) para incluir uma mensagem no registro de ponto.',
  })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        photo: {
          type: 'string',
          format: 'binary',
          description: 'Arquivo de imagem para validação facial (obrigatório)',
        },
        message: {
          type: 'string',
          nullable: true,
          description: 'Mensagem opcional para incluir no registro de ponto',
        },
      },
      required: ['photo'],
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Ponto registrado com sucesso',
    type: TimeClock,
  })
  @ApiResponse({
    status: 400,
    description:
      'Requisição inválida - arquivo de imagem ausente ou face não validada',
  })
  @ApiResponse({
    status: 401,
    description: 'Token JWT inválido ou expirado',
  })
  @ApiResponse({
    status: 500,
    description: 'Erro interno do servidor ou erro na comunicação com CompreFace',
  })
  @Post('mobile')
  @UseInterceptors(FileInterceptor('photo'))
  async clockInMobile(
    @UserId() userId: string,
    @UploadedFile() photo: Express.Multer.File,
    @Body('message') message?: string,
  ): Promise<TimeClock> {
    try {
      // Verificar se o usuário existe
      await this.usersService.findById(userId);

      // Verificar se o arquivo foi enviado
      if (!photo) {
        throw new HttpException(
          'Arquivo de imagem é obrigatório',
          HttpStatus.BAD_REQUEST,
        );
      }

      // Bater ponto (valida a face internamente)
      const timeClock = await this.timeClockService.createWithFaceVerification(
        userId,
        photo,
        message,
      );

      return timeClock;
    } catch (error) {
      if (error instanceof HttpException) {
        throw error;
      }
      throw new HttpException(
        `Erro ao bater ponto: ${error.message}`,
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}
