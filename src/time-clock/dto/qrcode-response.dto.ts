import { ApiProperty } from '@nestjs/swagger';

export class QrCodeResponse {
  @ApiProperty({
    description: 'QR code em formato Base64 (PNG)',
    example: 'iVBORw0KGgoAAAANSUhEUgAA...',
  })
  qrCodeBase64: string;

  @ApiProperty({
    description: 'URL que será aberta ao escanear o QR code',
    example: 'http://localhost:3000/timeclock/mobile?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  url: string;

  @ApiProperty({
    description: 'Token temporário do QR code (para referência)',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  token: string;

  @ApiProperty({
    description: 'Tempo de expiração do token em minutos',
    example: 15,
  })
  expiresInMinutes: number;
}
