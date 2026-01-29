import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Permission } from '../entities/permission.entity';
import { PermissionTemplate } from '../entities/permission-template.entity';

@Injectable()
export class PermissionsService {
  constructor(
    @InjectRepository(Permission)
    private permissionRepository: Repository<Permission>,
    @InjectRepository(PermissionTemplate)
    private permissionTemplateRepository: Repository<PermissionTemplate>,
  ) {}

  async findByUser(userId: string): Promise<Permission[]> {
    return this.permissionRepository.find({
      where: { user: { id: userId } },
    });
  }

  async findAllTemplates(): Promise<PermissionTemplate[]> {
    return this.permissionTemplateRepository.find();
  }

  async createTemplate(
    templateData: Partial<PermissionTemplate>,
  ): Promise<PermissionTemplate> {
    const template = this.permissionTemplateRepository.create({
      ...templateData,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    });

    return this.permissionTemplateRepository.save(template);
  }
}
