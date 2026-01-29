package com.rh360.rh360.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class UserPermissionRequestDeserializer extends JsonDeserializer<UserPermissionRequest> {

    @Override
    public UserPermissionRequest deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        
        UserPermissionRequest request = new UserPermissionRequest();
        
        // Se for uma string simples, usar como function e isPermitted = true
        if (node.isTextual()) {
            request.setFunction(node.asText());
            request.setIsPermitted(true);
        } 
        // Se for um objeto, deserializar normalmente
        else if (node.isObject()) {
            if (node.has("function")) {
                request.setFunction(node.get("function").asText());
            }
            if (node.has("isPermitted")) {
                request.setIsPermitted(node.get("isPermitted").asBoolean());
            } else {
                // Se não tiver isPermitted, assume true por padrão
                request.setIsPermitted(true);
            }
        }
        
        return request;
    }
}
