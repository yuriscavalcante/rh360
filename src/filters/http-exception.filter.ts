import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';

@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(HttpExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    let status: number;
    let message: string;
    let error: string;
    let stack: string | undefined;

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      const exceptionResponse = exception.getResponse();
      
      if (typeof exceptionResponse === 'string') {
        message = exceptionResponse;
        error = exception.name;
      } else if (typeof exceptionResponse === 'object' && exceptionResponse !== null) {
        const responseObj = exceptionResponse as any;
        // Tenta pegar a mensagem de diferentes formatos possíveis
        message = responseObj.message || responseObj.error || exception.message || 'Erro interno do servidor';
        // Se message é um array (validação do NestJS), pega o primeiro item
        if (Array.isArray(message)) {
          message = message[0] || 'Erro de validação';
        }
        error = responseObj.error || exception.name;
      } else {
        message = exception.message || 'Erro interno do servidor';
        error = exception.name;
      }
    } else if (exception instanceof Error) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      message = exception.message || 'Erro interno do servidor';
      error = exception.name || 'Error';
      stack = exception.stack;
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      message = 'Erro interno do servidor';
      error = 'InternalServerError';
    }

    const errorResponse = {
      statusCode: status,
      timestamp: new Date().toISOString(),
      path: request.url,
      method: request.method,
      message,
      error,
    };

    // Log do erro
    const logMessage = `${request.method} ${request.url} - ${status} - ${message}`;
    
    if (status >= 500) {
      // Erros de servidor (500+) - logar com stack trace
      const errorStack = stack || (exception instanceof Error ? exception.stack : undefined);
      this.logger.error(
        logMessage,
        errorStack,
        {
          statusCode: status,
          path: request.url,
          method: request.method,
          body: request.body,
          query: request.query,
          params: request.params,
          user: (request as any).user?.id || (request as any).user?.email || 'N/A',
        },
      );
    } else if (status >= 400) {
      // Erros de cliente (400-499) - logar como warning
      this.logger.warn(logMessage, {
        statusCode: status,
        path: request.url,
        method: request.method,
        body: request.body,
        query: request.query,
        params: request.params,
        user: (request as any).user?.id || (request as any).user?.email || 'N/A',
      });
    }

    response.status(status).json(errorResponse);
  }
}
