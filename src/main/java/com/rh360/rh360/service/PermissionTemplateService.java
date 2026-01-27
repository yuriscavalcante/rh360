package com.rh360.rh360.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public List<PermissionTemplateResponse> findAll() {
        List<PermissionTemplate> templates = repository.findAll();
        logger.info("Listando {} templates de permissão", templates.size());
        return templates.stream()
            .map(PermissionTemplateResponse::new)
            .collect(Collectors.toList());
    }

}
