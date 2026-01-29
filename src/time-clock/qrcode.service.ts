import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as QRCode from 'qrcode';

@Injectable()
export class QrCodeService {
  constructor(private configService: ConfigService) {}

  /**
   * Gera um QR code em formato Base64 (PNG) para uma URL específica
   * 
   * @param url URL que será codificada no QR code
   * @returns Promise<string> String Base64 da imagem PNG do QR code
   */
  async generateQrCodeBase64(url: string): Promise<string> {
    try {
      const width = this.configService.get<number>('APP_QRCODE_WIDTH', 300);
      const height = this.configService.get<number>('APP_QRCODE_HEIGHT', 300);

      const qrCodeBase64 = await QRCode.toDataURL(url, {
        width: width,
        margin: 1,
        errorCorrectionLevel: 'M',
        type: 'image/png',
      });

      // Remover o prefixo "data:image/png;base64," do resultado
      return qrCodeBase64.replace(/^data:image\/png;base64,/, '');
    } catch (error) {
      throw new Error(`Erro ao gerar QR code: ${error.message}`);
    }
  }

  /**
   * Gera a URL que será usada no QR code para bater ponto no mobile
   * 
   * @param qrToken Token temporário para autenticação via QR code
   * @returns URL completa para acessar via mobile
   */
  generateQrCodeUrl(qrToken: string): string {
    const frontendUrl = this.configService.get<string>(
      'APP_FRONTEND_URL',
      'http://localhost:3000',
    );
    
    // Garantir que a URL não termina com /
    const baseUrl = frontendUrl.endsWith('/') 
      ? frontendUrl.slice(0, -1) 
      : frontendUrl;
    
    return `${baseUrl}/timeclock/mobile/qr?token=${qrToken}`;
  }

  /**
   * Gera a URL que será usada no QR code com ID e path customizados
   * 
   * @param id ID que será incluído na URL
   * @param path Path para onde o QR code será redirecionado
   * @param qrToken Token temporário para autenticação via QR code (opcional)
   * @returns URL completa para acessar via mobile
   */
  generateQrCodeUrlWithParams(
    id: string,
    path: string,
    qrToken?: string,
  ): string {
    const frontendUrl = this.configService.get<string>(
      'APP_FRONTEND_URL',
      'http://localhost:3000',
    );
    
    // Garantir que o path começa com /
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    
    // Garantir que a URL não termina com /
    const baseUrl = frontendUrl.endsWith('/') 
      ? frontendUrl.slice(0, -1) 
      : frontendUrl;
    
    // Construir URL completa com id e path
    let url = `${baseUrl}${normalizedPath}?id=${id}`;
    
    // Adicionar token se fornecido
    if (qrToken && qrToken.trim() !== '') {
      url += `&token=${qrToken}`;
    }
    
    return url;
  }
}
