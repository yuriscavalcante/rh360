package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh360.rh360.dto.PermissionTemplateRequest;
import com.rh360.rh360.dto.PermissionTemplateResponse;
import com.rh360.rh360.entity.PermissionTemplate;
import com.rh360.rh360.repository.PermissionTemplateRepository;

@Service
public class PermissionTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionTemplateService.class);
    
    private final PermissionTemplateRepository repository;

    public PermissionTemplateService(PermissionTemplateRepository repository) {
        this.repository = repository;
    }

    public PermissionTemplateResponse create(PermissionTemplateRequest request) {
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome do template é obrigatório");
        }
        if (request.getLabel() == null || request.getLabel().trim().isEmpty()) {
            throw new RuntimeException("Label do template é obrigatório");
        }
        if (request.getRota() == null || request.getRota().trim().isEmpty()) {
            throw new RuntimeException("Rota do template é obrigatória");
        }

        PermissionTemplate template = new PermissionTemplate();
        template.setNome(request.getNome().trim());
        template.setLabel(request.getLabel().trim());
        template.setRota(request.getRota().trim());
        template.setCreatedAt(LocalDateTime.now().toString());
        template.setUpdatedAt(LocalDateTime.now().toString());

        PermissionTemplate savedTemplate = repository.save(template);
        logger.info("Template de permissão {} criado com sucesso", savedTemplate.getId());
        
        return new PermissionTemplateResponse(savedTemplate);
    }

    public List<PermissionTemplateResponse> findAll() {
        return findAll(null);
    }

    public List<PermissionTemplateResponse> findAll(String search) {
        List<PermissionTemplate> templates;
        if (search != null && !search.trim().isEmpty()) {
            templates = repository.findByNomeOrLabelContainingIgnoreCase(search.trim());
        } else {
            templates = repository.findAll();
        }
        logger.info("Listando {} templates de permissão", templates.size());
        return templates.stream()
            .map(PermissionTemplateResponse::new)
            .collect(Collectors.toList());
    }

}
