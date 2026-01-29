import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Like } from 'typeorm';
import { PermissionTemplate } from '../entities/permission-template.entity';
import { PermissionTemplateRequest } from './dto/permission-template-request.dto';
import { PermissionTemplateResponse } from './dto/permission-template-response.dto';

@Injectable()
export class PermissionTemplatesService {
  constructor(
    @InjectRepository(PermissionTemplate)
    private repository: Repository<PermissionTemplate>,
  ) {}

  async create(
    request: PermissionTemplateRequest,
  ): Promise<PermissionTemplateResponse> {
    if (!request.nome || !request.nome.trim()) {
      throw new Error('Nome do template é obrigatório');
    }
    if (!request.label || !request.label.trim()) {
      throw new Error('Label do template é obrigatório');
    }
    if (!request.rota || !request.rota.trim()) {
      throw new Error('Rota do template é obrigatória');
    }

    const template = new PermissionTemplate();
    template.nome = request.nome.trim();
    template.label = request.label.trim();
    template.rota = request.rota.trim();
    template.createdAt = new Date().toISOString();
    template.updatedAt = new Date().toISOString();

    const saved = await this.repository.save(template);
    return this.toResponse(saved);
  }

  async findAll(search?: string): Promise<PermissionTemplateResponse[]> {
    let templates: PermissionTemplate[];
    if (search && search.trim()) {
      templates = await this.repository.find({
        where: [
          { nome: Like(`%${search.trim()}%`) },
          { label: Like(`%${search.trim()}%`) },
        ],
      });
    } else {
      templates = await this.repository.find();
    }
    return templates.map((t) => this.toResponse(t));
  }

  private toResponse(template: PermissionTemplate): PermissionTemplateResponse {
    return {
      id: template.id,
      nome: template.nome,
      label: template.label,
      rota: template.rota,
      createdAt: template.createdAt,
      updatedAt: template.updatedAt,
    };
  }
}
