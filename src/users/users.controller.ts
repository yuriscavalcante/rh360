import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  UseInterceptors,
  UploadedFile,
  HttpStatus,
  HttpException,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiConsumes,
  ApiBody,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';
import { UsersService } from './users.service';
import { UserRequestDto } from './dto/user-request.dto';
import { UserResponseDto } from './dto/user-response.dto';

@ApiTags('Usuários')
@Controller('api/users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @ApiOperation({ summary: 'Criar novo usuário' })
  @ApiResponse({
    status: 200,
    description: 'Usuário criado com sucesso',
    type: UserResponseDto,
  })
  @Post()
  async create(@Body() userDto: UserRequestDto): Promise<UserResponseDto> {
    try {
      return await this.usersService.create(userDto);
    } catch (error) {
      throw new HttpException(
        { error: error.message },
        error.status || HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @ApiOperation({ summary: 'Criar novo usuário com foto' })
  @ApiConsumes('multipart/form-data')
  @ApiBody({ type: UserRequestDto })
  @Post('with-photo')
  @UseInterceptors(FileInterceptor('photo'))
  async createWithPhoto(
    @Body() userDto: UserRequestDto,
    @UploadedFile() photo?: Express.Multer.File,
  ): Promise<UserResponseDto> {
    try {
      return await this.usersService.create(userDto, photo);
    } catch (error) {
      throw new HttpException(
        { error: error.message },
        error.status || HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @ApiOperation({ summary: 'Listar todos os usuários' })
  @ApiBearerAuth()
  @ApiResponse({
    status: 200,
    description: 'Lista de usuários retornada com sucesso',
  })
  @UseGuards(JwtAuthGuard)
  @Get()
  async findAll(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20',
    @Query('search') search?: string,
  ) {
    return await this.usersService.findAll(
      parseInt(page),
      parseInt(size),
      search,
    );
  }

  @ApiOperation({ summary: 'Buscar usuário por ID' })
  @ApiBearerAuth()
  @ApiResponse({
    status: 200,
    description: 'Usuário encontrado',
    type: UserResponseDto,
  })
  @UseGuards(JwtAuthGuard)
  @Get(':id')
  async findById(@Param('id') id: string): Promise<UserResponseDto> {
    return await this.usersService.findById(id);
  }

  @ApiOperation({ summary: 'Obter usuário atual' })
  @ApiBearerAuth()
  @ApiResponse({
    status: 200,
    description: 'Dados do usuário atual retornados com sucesso',
    type: UserResponseDto,
  })
  @UseGuards(JwtAuthGuard)
  @Get('me')
  async getCurrentUser(@UserId() userId: string): Promise<UserResponseDto> {
    return await this.usersService.findById(userId);
  }

  @ApiOperation({ summary: 'Atualizar usuário' })
  @ApiBearerAuth()
  @ApiResponse({
    status: 200,
    description: 'Usuário atualizado com sucesso',
    type: UserResponseDto,
  })
  @UseGuards(JwtAuthGuard)
  @Put(':id')
  async update(
    @Param('id') id: string,
    @Body() userDto: UserRequestDto,
  ): Promise<UserResponseDto> {
    try {
      return await this.usersService.update(id, userDto);
    } catch (error) {
      throw new HttpException(
        { error: error.message },
        error.status || HttpStatus.BAD_REQUEST,
      );
    }
  }

  @ApiOperation({ summary: 'Deletar usuário' })
  @ApiBearerAuth()
  @ApiResponse({ status: 200, description: 'Usuário deletado com sucesso' })
  @UseGuards(JwtAuthGuard)
  @Delete(':id')
  async delete(@Param('id') id: string): Promise<void> {
    try {
      await this.usersService.delete(id);
    } catch (error) {
      throw new HttpException(
        { error: error.message },
        error.status || HttpStatus.BAD_REQUEST,
      );
    }
  }
}
